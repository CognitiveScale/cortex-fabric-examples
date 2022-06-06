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
import com.c12e.cortex.phoenix.spark.DataSource;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import picocli.CommandLine;

import java.util.HashMap;

@CommandLine.Command(name = "ds-rw", description = "Example DataSource Read and Write", mixinStandardHelpOptions = true)
public class DataSourceRW extends  BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @CommandLine.Option(names = {"-d", "--data-source"}, description = "DataSource Name", required = true)
    String dataSourceName;



    @CommandLine.Spec
    private CommandLine.Model.CommandSpec cmdSpec;



    /**
     * Code to run for a command
     */
    @Override
    public void run() {
        //get spark session
        SparkSession session = getSparkSession(getDefaultProps());

        //create local secrets map for use in non-cluster env
        LocalSecretClient.LocalSecrets localSecrets = new LocalSecretClient.LocalSecrets();
        localSecrets.setSecretsForProject("mctest30", new HashMap() {{
                    put("aws-secret", System.getenv("CONN_AWS_SECRET"));
                }}
        );

        //create fabric session
        FabricSession fabricSession = getFabricSession(session, localSecrets);

        //read data source
        DataSource dataSource = fabricSession.catalog().getDataSource(project, dataSourceName);


        //load the dataset from the connection
        Dataset<Row> connDs = fabricSession.read().readConnection(project, dataSource.getConnection().getName()).load();

        //write to the datasource
        fabricSession.write().writeDataSource(connDs, project, dataSourceName).save();

        //read from the datasource
        Dataset<Row> dataSourceDs = fabricSession.read().readDataSource(project, dataSourceName).load();

        //do some verification
    }
}


