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

import com.c12e.cortex.phoenix.ProfileSchema;
import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.profiles.client.LocalSecretClient;
import com.c12e.cortex.profiles.intercept.GraphingRequestInterceptor;
import com.c12e.cortex.profiles.intercept.TracingTimingMethodInterceptor;
import com.c12e.cortex.profiles.module.CortexDeltaMergeBuilder;
import com.c12e.cortex.profiles.module.job.BuildProfileJob;
import com.c12e.cortex.profiles.module.job.IngestDataSourceJob;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.delta.tables.DeltaTable;
import org.apache.spark.sql.*;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@CommandLine.Command(name = "demo-m2", description = "Demo For Milestone 2", mixinStandardHelpOptions = true)
public class DemoMilestone2 extends  BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @CommandLine.Option(names = {"-ps", "--profile-schema"}, description = "ProfileSchema Name", required = true)
    String profileSchemaName;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec cmdSpec;

    public static final String PROFILE_REFRESH_WITH_PARTITION = "spark.cortex.refreshKey";
    public static final String PROFILE_SKIP_FEATURE_CATALOG_ANALYTICS = "spark.cortex.skipAnalyticsKey";


    /**
     * Code to run for a command
     */
    @Override
    public void run() {
        Map<String, String> defaultOptions = getDefaultProps();
        defaultOptions.put(CortexSession.METHOD_PROXY_KEY, TracingTimingMethodInterceptor.class.getName());
        defaultOptions.put(PROFILE_REFRESH_WITH_PARTITION, "false");
        defaultOptions.put(PROFILE_SKIP_FEATURE_CATALOG_ANALYTICS, "true");

        //get spark session
        SparkSession session = getSparkSession(defaultOptions);

        LocalSecretClient.LocalSecrets localSecrets = new LocalSecretClient.LocalSecrets();
        localSecrets.setSecretsForProject(project, new HashMap<String, String>() {{
                    put("aws-secret", "myPassword");
                }}
        );

        //create cortex session
        CortexSession cortexSession = getCortexSession(session, localSecrets);

        //get profileschema from the catalog
        ProfileSchema profileSchema = cortexSession.catalog().getProfileSchema(project, profileSchemaName);

        String stateCode = "KY";
        Function<Dataset<Row>, Dataset<Row>> filterState = (ds) -> ds.filter(ds.col("state_code").equalTo(stateCode));

        //build primary datasource
        IngestDataSourceJob ingestMemberBase = cortexSession.job().ingestDataSource(project, profileSchema.getPrimarySource().getName(), cortexSession.getContext());


        //ingestMemberBase.formatDatasetForDataSource = (ds) -> ds.filter(ds.col("state_code").equalTo("KY"));
        //ingestMemberBase.defaultWriterOptions = (dsw) -> dsw.mode(SaveMode.Overwrite);


        //ingestMemberBase.formatDatasetForDataSource = (ds) -> ds.filter(ds.col("state_code").equalTo(session.sqlContext().getConf("STATE_KEY")));


        /*
        for(String state : Arrays.asList("KY", "CA", "NY")) {
            ingestMemberBase.formatDatasetForDataSource = (ds) -> ds.filter(ds.col("state_code").equalTo(state));
            ingestMemberBase.run();
        }
        */
        /*
        List<Row> distinctStateCodes = cortexSession.read()
                .connection(project,
                        cortexSession.catalog().getDataSource(project,
                                profileSchema.getPrimarySource().getName()).getConnection().getName()).load()
                .select("state_code").distinct().collectAsList();

        distinctStateCodes.stream().map(row -> row.getString(0)).forEach((state) -> {
            ingestMemberBase.formatDatasetForDataSource = (ds) -> ds.filter(ds.col("state_code").equalTo(session.sqlContext().getConf(state)));
            ingestMemberBase.run();
        });
        */


       // ingestMemberBase.formatDatasetForDataSource = filterState;
        ingestMemberBase.run();


        //build all joined datasources
        profileSchema.getJoins().forEach(join -> {
            IngestDataSourceJob ingestJoin = cortexSession.job().ingestDataSource(project, join.getName(), cortexSession.getContext());
            ingestJoin.run();
        });

        //build profile
        BuildProfileJob buildProfileJob = cortexSession.job().buildProfile(project, profileSchemaName, cortexSession.getContext());

        if("true".equals(session.sqlContext().getConf(PROFILE_SKIP_FEATURE_CATALOG_ANALYTICS))) {
            buildProfileJob.performFeatureCatalogCalculations = () -> false;
        }

        if("true".equals(session.sqlContext().getConf(PROFILE_REFRESH_WITH_PARTITION))) {
            buildProfileJob.writerOptions = (dsw) -> dsw
                    .deltaMerge(null)
                    .option("overwriteSchema", "true")
                    .mode(SaveMode.Overwrite)
                    .partitionBy("state_code", "zip_code");
        }

        /*
        CortexDeltaMergeBuilder mergeBuilder = CortexDeltaMergeBuilder.getInstance();
        mergeBuilder.onCondition = (builder) ->
                String.format("existing.state_code = 'KY' and existing.%s = incoming.%s",
                        builder.targetPrimaryKey, builder.sourcePrimaryKey);

        mergeBuilder.merge = (builder) -> {
            System.out.println(filterState.apply(builder.ds).count());
            return builder.deltaTable.alias(builder.existing)
                    .merge(
                            filterState.apply(builder.ds).alias(builder.incoming),
                            builder.onCondition.apply(builder)
                    );
        };

        buildProfileJob.defaultWriterOptions = (dsw) -> dsw.deltaMerge(mergeBuilder);
*/
        buildProfileJob.run();
    }
}
