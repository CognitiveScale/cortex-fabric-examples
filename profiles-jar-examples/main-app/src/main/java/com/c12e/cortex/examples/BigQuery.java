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
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import picocli.CommandLine;

@CommandLine.Command(name = "bigquery", description = "Example Spark Bigquery Connection", mixinStandardHelpOptions = true)
public class BigQuery extends  BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @CommandLine.Option(names = {"-i", "--input"}, description = "Connection to read from", required = true)
    String source;

    @CommandLine.Option(names = {"-o", "--output"}, description = "Connection to write to", required = true)
    String sink;

    //TODO Bigquery table? command option

    //TODO cred file location


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
        //create fabric session
        FabricSession fabricSession = FabricSession.newSession(session);
        //read from bigquery table
        Dataset<Row> ds = session.read().format("bigquery").option("parentProject", "fabric-qa")
                .option("credentials", System.getenv("BIGQUERY_CRED"))
                .option("table", "bigquery-public-data.samples.shakespeare").load();

        fabricSession.write().writeConnection(ds, project, sink).mode(SaveMode.Overwrite).save();

    }
}
