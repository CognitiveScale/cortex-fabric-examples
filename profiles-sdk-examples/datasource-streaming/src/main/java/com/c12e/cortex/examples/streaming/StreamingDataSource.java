/*
 * Copyright 2022 Cognitive Scale, Inc. All Rights Reserved.
 *
 *  See LICENSE.txt for details.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.c12e.cortex.examples.streaming;

import com.c12e.cortex.phoenix.DataSource;
import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.profiles.module.CortexDataSourcePair;
import com.c12e.cortex.profiles.module.CortexDeltaMergeBuilder;
import com.c12e.cortex.profiles.module.datasource.CortexDataSourceStreamWriter;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQueryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.TimeoutException;

import com.c12e.cortex.examples.local.SessionExample;

/**
 * Example CLI application that refreshes a Data Source via streaming (reading and writing).
 */
@Command(name = "ds-streaming", description = "Example Streaming Data Source", mixinStandardHelpOptions = true)
public class StreamingDataSource implements Runnable {
    @Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @Option(names = {"-d", "--data-source"}, description = "Data Source Name", required = true)
    String dataSourceName;

    Logger logger = LoggerFactory.getLogger(StreamingDataSource.class);

    @Override
    public void run() {
        SessionExample sessionExample = new SessionExample();
        CortexSession cortexSession = sessionExample.getCortexSession();
        streamDataSource(cortexSession);
    }

    public void streamDataSource(CortexSession cortexSession) {
        // Get the SparkSession
        SparkSession session = cortexSession.spark();

        // Attach a custom listener to steaming process
        TestStreamQueryListener queryListener = new TestStreamQueryListener(session);
        session.streams().addListener(queryListener);

        // Get the Data Source from the Catalog. This is done to get a back-reference to the underlying Connection
        DataSource dataSource = cortexSession.catalog().getDataSource(project, dataSourceName);

        // Read (stream) the Data Source. This returns a CortexDataSourcePair, which holds a static and streaming
        // representation of the Data Source.
        CortexDataSourcePair dsPair = cortexSession.readStream().readConnection(project, dataSource.getConnection().getName()).load();

        // Log information about static sample
        Dataset<Row> staticDf = dsPair.getStaticDf();
        logger.info("Static dataframe has {} rows", staticDf.count());

        // Write (stream) the Data Source. Skip performing aggregation on micro-batches when writing the Data Source.
        logger.info("Starting stream");
        CortexDataSourceStreamWriter writer = cortexSession.writeStream()
                .writeDataSource(dsPair, project, dataSourceName)
                .performAggregation(false);

        // Don't perform feature catalog calculations during write. This saves times in re-computing feature information
        // (e.g. name, min, max, etc.).
        writer.performFeatureCatalogCalculations(false);

        // Perform a Delta Table merge when writing the data.
        writer.deltaMerge(CortexDeltaMergeBuilder.getInstance());
        //writer.mode(SaveMode.Overwrite);
        //writer.mode(SaveMode.Append);

        writer.save();
        logger.info("Finished process");
    }

    /**
     * Streaming Listener that the streaming query state.
     */
    public class TestStreamQueryListener extends StreamingQueryListener {
        SparkSession sparkSession;
        Long countBeforeStop = 3L;

        public TestStreamQueryListener(SparkSession sparkSession) {
            this.sparkSession = sparkSession;
        }

        @Override
        public void onQueryStarted(QueryStartedEvent event) {
            logger.info("STREAMING LISTENER: Streaming Query started");
        }

        @Override
        public void onQueryProgress(QueryProgressEvent event) {
            logger.info("STREAMING LISTENER: Streaming Query in progress");
            if (event.progress().numInputRows() == 0) {
                countBeforeStop--;
                if(countBeforeStop == 0){
                    logger.info("STREAMING LISTENER: Initiating Streaming Query stop");
                    try {
                        sparkSession.sqlContext().streams().get(event.progress().id()).stop();
                    } catch (TimeoutException e) {
                        logger.error("STREAMING LISTENER: Timeout error in query", e);
                    }
                }
            }
            logger.info(event.progress().prettyJson());
            logger.info("STREAMING LISTENER: No processing occurred in last poll, stopping in {} poll intervals", countBeforeStop);
        }

        @Override
        public void onQueryTerminated(QueryTerminatedEvent event) {
            logger.info("STREAMING LISTENER: onQueryTerminated");
        }
    }
}


