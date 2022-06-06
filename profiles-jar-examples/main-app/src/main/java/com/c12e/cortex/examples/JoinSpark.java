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

@CommandLine.Command(name = "join-spark", description = "Example Joining Connections", mixinStandardHelpOptions = true)
public class JoinSpark extends BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @CommandLine.Option(names = {"-l", "--left-loc"}, description = "Left Join Side File", required = true)
    String leftLocation;

    @CommandLine.Option(names = {"-r", "--right-loc"}, description = "Right Join Side File", required = true)
    String rightLocation;

    @CommandLine.Option(names = {"-w", "--write-loc"}, description = "Write File", required = true)
    String writeLocation;
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec cmdSpec;


    @Override
    public void run() {
        SparkSession session = getSparkSession(getDefaultProps());
        FabricSession fabricSession = FabricSession.newSession(session);
        Dataset<Row> leftConn = session.read().csv(leftLocation);
        Dataset<Row> rightConn = session.read().csv(rightLocation);

        //inner join
        Dataset<Row> ds = leftConn.join(rightConn, "member_id");
        fabricSession.write().writeConnection(ds, project, "member-base-file")
                .mode(SaveMode.Overwrite).option("uri", writeLocation).save();
    }
}


