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

@CommandLine.Command(name = "cdata", description = "Example CData Connection", mixinStandardHelpOptions = true)
public class CData extends  BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @CommandLine.Option(names = {"-i", "--input"}, description = "Connection to read from", required = true)
    String source;

    @CommandLine.Option(names = {"-o", "--output"}, description = "Connection to write to", required = true)
    String sink;

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
        localSecrets.setSecretsForProject(project, new HashMap() {{
                    put("oem_key", System.getenv("CDATA_OEM_KEY"));
                }}
        );
        System.setProperty("product_checksum", System.getenv("CDATA_PRODUCT_CHECKSUM"));

        //create fabric session
        FabricSession fabricSession = getFabricSession(session, localSecrets);
        //read connection
        Dataset<Row> ds = fabricSession.read().readConnection(project, source).load();

        //TODO Perform some type of transform

        //write to a datasource
        fabricSession.write().writeConnection(ds, project, sink).mode(SaveMode.Overwrite).save();

        //TODO add CDATA jar to build dependencies

        //TODO whatever is easiest...salesforce?

    }
}
