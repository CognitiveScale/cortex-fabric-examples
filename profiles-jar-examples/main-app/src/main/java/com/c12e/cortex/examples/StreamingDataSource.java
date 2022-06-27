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

package com.c12e.cortex.examples;

import com.c12e.cortex.phoenix.DataSource;
import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.profiles.client.LocalSecretClient;
import com.c12e.cortex.profiles.module.CortexDataSourcePair;
import com.c12e.cortex.profiles.module.CortexDeltaMergeBuilder;
import com.c12e.cortex.profiles.module.datasource.CortexDataSourceStreamWriter;
import io.delta.tables.DeltaTable;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQueryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

@CommandLine.Command(name = "ds-streaming", description = "Example Streaming DataSource", mixinStandardHelpOptions = true)
public class StreamingDataSource extends  BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @CommandLine.Option(names = {"-d", "--data-source"}, description = "DataSource Name", required = true)
    String dataSourceName;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec cmdSpec;

    private static final String AWS_CONNECTION_SECRET_ENV = "CONN_AWS_SECRET";

    private static void checkRequiredSecrets() {
        if (System.getenv(AWS_CONNECTION_SECRET_ENV) == null) {
            System.err.println(String.format("Missing environment variable '%s' for local secrets client", AWS_CONNECTION_SECRET_ENV));
            System.exit(2);
        }
    }

    Logger logger = LoggerFactory.getLogger(StreamingDataSource.class);

    /**
     * Code to run for a command
     */
    @Override
    public void run() {
        //get spark session
        SparkSession session = getSparkSession(getDefaultProps());

        LocalSecretClient.LocalSecrets localSecrets = new LocalSecretClient.LocalSecrets();
        localSecrets.setSecretsForProject(project, new HashMap() {{
                    put("aws-secret", "none");
                }}
        );

        //create cortex session
        CortexSession cortexSession = getCortexSession(session, localSecrets);

        //start listener to stop process
        TestStreamQueryListener queryListener = new TestStreamQueryListener(session);
        session.streams().addListener(queryListener);

        //read data source
        DataSource dataSource = cortexSession.catalog().getDataSource(project, dataSourceName);

        CortexDataSourcePair dsPair = cortexSession.readStream().readConnection(project, dataSource.getConnection().getName()).load();

        logger.info("Starting stream");
        CortexDataSourceStreamWriter writer = cortexSession.writeStream().writeDataSource(dsPair, project, dataSourceName);

        //don't perform feature catalog calculations
        writer.performFeatureCatalogCalculations(false);
        //don't perform groupBy on micro-batch
        writer.performAggregation(false);

        //supported modes
        writer.deltaMerge(CortexDeltaMergeBuilder.getInstance());
        //writer.mode(SaveMode.Overwrite);
        //writer.mode(SaveMode.Append);

        writer.save();


        logger.info("Finished process");
    }

    public class TestStreamQueryListener extends  StreamingQueryListener {
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


