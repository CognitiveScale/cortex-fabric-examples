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

@CommandLine.Command(name = "join-conns", description = "Example Joining Connections", mixinStandardHelpOptions = true)
public class JoinConnections extends  BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @CommandLine.Option(names = {"-l", "--left-conn"}, description = "Left Join Side Connection Name", required = true)
    String leftConnection;

    @CommandLine.Option(names = {"-r", "--right-conn"}, description = "Right Join Side Connection Name", required = true)
    String rightConnection;

    @CommandLine.Option(names = {"-w", "--write-conn"}, description = "Write Connection Name", required = true)
    String writeConnection;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec cmdSpec;

    private static final String AWS_CONNECTION_SECRET_ENV = "CONN_AWS_SECRET";



    /**
     * Code to run for a command
     */
    @Override
    public void run() {
        //get spark session
        SparkSession session = getSparkSession(getDefaultProps());
        session.conf().getAll().toStream().print();

        //create local secrets map for use in non-cluster env
        LocalSecretClient.LocalSecrets localSecrets = new LocalSecretClient.LocalSecrets();
        localSecrets.setSecretsForProject(project, new HashMap() {{
                    put("aws-secret", System.getenv(AWS_CONNECTION_SECRET_ENV));
                }}
        );

        //create cortex session
        CortexSession cortexSession = getCortexSession(session, localSecrets);
        //load the two connections from cortex through the api-server
        Dataset<Row> leftConn = cortexSession.read().connection(project, leftConnection).load();
        Dataset<Row> rightConn = cortexSession.read().connection(project, rightConnection).load();

        //inner join
        Dataset<Row> ds = leftConn.join(rightConn, "member_id");



        //write to a third connection as a data sink
        cortexSession.write().connection(ds, project, writeConnection).mode(SaveMode.Overwrite).save();
    }
}


