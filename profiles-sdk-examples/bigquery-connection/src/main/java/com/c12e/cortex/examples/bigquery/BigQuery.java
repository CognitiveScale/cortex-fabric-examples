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
import com.c12e.cortex.phoenix.InternalSecretsClient;
import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.profiles.client.CortexSecretsClient;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Example CLI application reading data from BigQuery and writing the result to a Cortex Connection.
 */
@Command(name = "bigquery", description = "Example writing to a Cortex Connection from Google Bigquery with Spark", mixinStandardHelpOptions = true)
public class BigQuery implements Runnable {
    @Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @Option(names = {"-o", "--output"}, description = "Connection to write to", required = true)
    String sink;

    @Option(names = {"-gp", "--google-project"}, description = "Parent Project for BigQuery table", required = true)
    String parentProject;

    @Option(names = {"-t", "--table"}, description = "BigQuery table name", required = true)
    String tableName;

    @Option(names = {"-s", "--secret"}, description = "Cortex Secret Key to use for BigQuery Credentials. Optional if the 'BIGQUERY_CREDS_FILE' environment variable is set.")
    String secretKey;

    public static final String BIGQUERY_CREDS_FILE = "BIGQUERY_CREDS_FILE";

    /**
     * Retrieves the BigQuery credential from a Cortex Secret. Only applicable when running in the cluster.
     * @param session Cortex Session
     * @return Secret value
     * @throws RuntimeException If the Secret doesn't exist.
     */
    private String getBigqueryCredsFromSecret(CortexSession session) {
        if (secretKey == null) {
            throw new RuntimeException("Missing Secret name for BigQuery credentials. Try setting '--secret'");
        }
        // Retrieve the Cortex Token and Secrets URL from the Spark Configuration
        String token = session.spark().conf().get(CortexSession.PHOENIX_TOKEN_KEY);
        String url = session.spark().conf().get(CortexSession.SECRET_CLIENT_URL_KEY);

        // Retrieve the Secret.
        var client = new InternalSecretsClient(url, token);
        var secret = client.getSecret(project, secretKey);
        System.out.println(String.format("Secret (%s): '%s'", secretKey, secret));
        return secret;
    }

    private String getBigQueryCredsFromEnv() {
        var credentials = System.getenv(BIGQUERY_CREDS_FILE);
        if (credentials == null) {
            throw new RuntimeException(String.format("Missing BigQuery JSON credentials. Try setting (environment variable): '%s'", BIGQUERY_CREDS_FILE));
        }
        return credentials;
    }

    private boolean useCredentialsFile(CortexSession session) {
        // Check if Secret Client URL is set in the Spark Configuration. If not, then use credentials file.
        try {
            session.spark().conf().get(CortexSession.SECRET_CLIENT_URL_KEY);
        } catch (NoSuchElementException ne) {
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        // Create cortex session
        var sessionExample = new SessionExample();
        CortexSession cortexSession = sessionExample.getCortexSession();
        writeFromBigQuery(cortexSession);
    }

    public void writeFromBigQuery(CortexSession cortexSession) {
        // Get spark session
        SparkSession session = cortexSession.spark();
        session.conf().getAll().toStream().print();

        // Read the BigQuery table
        var reader = session.read()
                .format("bigquery")
                .option("parentProject", parentProject)
                .option("table", tableName);
        // Apply credentials either via file (local use) or Secret (in cluster)
        if (useCredentialsFile(cortexSession)) {
            reader = reader.option("credentialsFile", getBigQueryCredsFromEnv());
        } else {
            reader = reader.option("credentials", getBigqueryCredsFromSecret(cortexSession));
        }
        Dataset<Row> ds = reader.load();

        // Show the first row
        ds.show(1);

        // Write result. Note `SaveMode.Overwrite` will cause the bigquery table to override any existing
        // data in the sink
        cortexSession.write()
                .connection(ds, project, sink)
                .mode(SaveMode.Overwrite)
                .save();
    }
}
