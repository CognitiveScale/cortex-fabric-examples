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

package com.c12e.cortex.examples.bigquery;

import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.profiles.CortexSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Example CLI application reading data from BigQuery and writing the result to a Cortex Connection.
 */
@Command(name = "bigquery", description = "Example Spark Bigquery Connection", mixinStandardHelpOptions = true)
public class BigQuery implements Runnable {
    @Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @Option(names = {"-o", "--output"}, description = "Connection to write to", required = true)
    String sink;

    @Option(names = {"-gp", "--google-project"}, description = "Parent Project for BigQuery table", required = true)
    String parentProject;

    @Option(names = {"-t", "--table"}, description = "BigQuery table name", required = true)
    String tableName;

    public static final String BIGQUERY_CREDS_FILE = "BIGQUERY_CREDS_FILE";

    private String getBigQueryCreds() {
        var credentials = System.getenv(BIGQUERY_CREDS_FILE);
        if (credentials == null) {
            throw new RuntimeException(String.format("Missing BigQuery JSON credentials path (environment variable): '%s'", BIGQUERY_CREDS_FILE));
        }
        return credentials;
    }

    @Override
    public void run() {
        // Create cortex session.
        var sessionExample = new SessionExample();
        CortexSession cortexSession = sessionExample.getCortexSession();
        writeFromBigQuery(cortexSession);
    }

    public void writeFromBigQuery(CortexSession cortexSession) {
        // Get spark session.
        SparkSession session = cortexSession.spark();
        session.conf().getAll().toStream().print();

        // Read from bigquery table.
        Dataset<Row> ds = session.read()
                .format("bigquery")
                .option("parentProject", parentProject)
                .option("credentialsFile", getBigQueryCreds())
                .option("table", tableName)
                .load();

        // Show the first row.
        ds.show(1);

        // Write result. Note `SaveMode.Overwrite` will cause the bigquery table to override any existing
        // data in the sink.
        cortexSession.write()
                .connection(ds, project, sink)
                .mode(SaveMode.Overwrite)
                .save();
    }
}
