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

import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.profiles.client.LocalSecretClient;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import picocli.CommandLine;

import java.util.HashMap;

@CommandLine.Command(name = "gen-graphs", description = "Example Generating Graphs", mixinStandardHelpOptions = true)
public class GenerateGraphs extends  BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @CommandLine.Option(names = {"-c", "--conn"}, description = "Connection Name", required = true)
    String connection;


    @CommandLine.Spec
    private CommandLine.Model.CommandSpec cmdSpec;

    private static final String AWS_CONNECTION_SECRET_ENV = "CONN_AWS_SECRET";

    private static void checkRequiredSecrets() {
        if (System.getenv(AWS_CONNECTION_SECRET_ENV) == null) {
            System.err.println(String.format("Missing environment variable '%s' for local secrets client", AWS_CONNECTION_SECRET_ENV));
            System.exit(2);
        }
    }

    /**
     * Code to run for a command
     */
    @Override
    public void run() {
        //get spark session
        SparkSession session = getSparkSession(getDefaultProps());
        session.conf().getAll().toStream().print();

        //create local secrets map for use in non-cluster env
        checkRequiredSecrets();
        LocalSecretClient.LocalSecrets localSecrets = new LocalSecretClient.LocalSecrets();
        localSecrets.setSecretsForProject(project, new HashMap() {{
                    put("aws-secret", System.getenv(AWS_CONNECTION_SECRET_ENV));
                }}
        );

        //create cortex session
        CortexSession cortexSession = getCortexSession(session, localSecrets);
        //load the two connections from cortex through the api-server
        Dataset<Row> ds = cortexSession.read().connection(project, connection).load();

        //write to a third connection as a data sink
        cortexSession.write().connection(ds, project, connection).mode(SaveMode.Overwrite).save();
    }
}


