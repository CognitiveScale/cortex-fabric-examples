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

import com.c12e.cortex.phoenix.profiles.spark.FabricSession;
import com.c12e.cortex.phoenix.profiles.spark.client.LocalSecretClient;
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
        localSecrets.setSecretsForProject("mctest30", new HashMap() {{
                    put("aws-secret", System.getenv("CONN_AWS_SECRET"));
                }}
        );

        //create fabric session
        FabricSession fabricSession = getFabricSession(session, localSecrets);
        //load the two connections from fabric through the api-server
        Dataset<Row> leftConn = fabricSession.read().readConnection(project, leftConnection).load();
        Dataset<Row> rightConn = fabricSession.read().readConnection(project, rightConnection).load();

        //inner join
        Dataset<Row> ds = leftConn.join(rightConn, "member_id");



        //write to a third connection as a data sink
        fabricSession.write().writeConnection(ds, project, writeConnection).mode(SaveMode.Overwrite).save();
    }
}


