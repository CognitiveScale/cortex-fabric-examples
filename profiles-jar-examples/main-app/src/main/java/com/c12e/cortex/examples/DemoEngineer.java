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

import com.c12e.cortex.generated.client.*;
import com.c12e.cortex.generated.types.ConnectionInput;
import com.c12e.cortex.generated.types.Feature;
import com.c12e.cortex.generated.types.FeatureInput;
import com.c12e.cortex.phoenix.*;
import com.c12e.cortex.phoenix.spec.ResourceRef;
import com.c12e.cortex.phoenix.spec.SourceKind;
import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.profiles.catalog.CortexRemoteCatalog;
import com.c12e.cortex.profiles.client.GeneratedPhoenixClient;
import com.c12e.cortex.profiles.client.LocalSecretClient;
import com.c12e.cortex.profiles.intercept.TracingTimingMethodInterceptor;
import com.c12e.cortex.profiles.logging.InjectLogger;
import com.c12e.cortex.profiles.module.CortexDeltaMergeBuilder;
import com.c12e.cortex.profiles.module.connection.CortexConnectionReader;
import com.c12e.cortex.profiles.module.datasource.CortexDataSourceWriter;
import com.c12e.cortex.profiles.module.job.BuildProfileJob;
import com.c12e.cortex.profiles.module.job.IngestDataSourceJob;
import com.google.inject.Inject;
import com.netflix.graphql.dgs.client.CustomGraphQLClient;
import com.netflix.graphql.dgs.client.GraphQLClient;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import picocli.CommandLine;

import javax.inject.Named;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@CommandLine.Command(name = "demo-eng", description = "Demo For Engineering Demo", mixinStandardHelpOptions = true)
public class DemoEngineer extends  BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @CommandLine.Option(names = {"-ps", "--profile-schema"}, description = "ProfileSchema Name", required = true)
    String profileSchemaName;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec cmdSpec;

    public static final String PROFILE_REFRESH_WITH_PARTITION = "spark.cortex.refreshKey";
    public static final String PROFILE_SKIP_FEATURE_CATALOG_ANALYTICS = "spark.cortex.skipAnalyticsKey";

    private void createAllResources(CortexSession cortexSession, HashMap<String, String> resourceMap, Boolean deleteFirst) {
        resourceMap.get("profileSchemaA");
        ProfileSchema profile = new ProfileSchema(null, null, null, null,
                null, null, null, null,null, null);
        //map json to catalog type
        if(deleteFirst) {
            cortexSession.catalog().deleteProfileSchema(profile.getProject(), profile.getName());
        }

        ProfileSchema foundSchema = cortexSession.catalog().getProfileSchema(profile.getProject(), profile.getName());

        //don't save if profile schema found, you could also update profile schema with new configuration
        //safest would be to delete first in that case
        if(foundSchema == null) {
            cortexSession.catalog().createProfileSchema(profile);
        }
    }

    /**
     * Code to run for a command
     */
    @Override
    public void run() {
        Map<String, String> defaultOptions = getDefaultProps();
        defaultOptions.put(CortexSession.METHOD_PROXY_KEY, TracingTimingMethodInterceptor.class.getName());
        defaultOptions.put(PROFILE_REFRESH_WITH_PARTITION, "false");
        defaultOptions.put(PROFILE_SKIP_FEATURE_CATALOG_ANALYTICS, "true");


        //hitting remote client, normally would only want to use this in cluster
        defaultOptions.put(CortexSession.PHOENIX_CLIENT_URL_KEY, "http://api.dci-dev.dev-eks.insights.ai");
        defaultOptions.put(CortexSession.PHOENIX_TOKEN_KEY, "eyJraWQiOiJfM1g1aWpvcGdTSm0tSmVmdWJQenh5RS1XWGw3UzJqSVZ" +
                "DLXRNWnNiRG9BIiwiYWxnIjoiRWREU0EifQ.eyJiZWFyZXIiOiJ1c2VyIiwiaWF0IjoxNjU2OTA0ODc4LCJleHAiOjE2NTY5OTEy" +
                "NzcsInN1YiI6IjI2ZTk2ZTU4LWRiOGMtNDk1ZC04Mjc5LWMyNDVjM2UyMzMwZSIsImF1ZCI6ImNvcnRleCIsImlzcyI6ImNvZ25" +
                "pdGl2ZXNjYWxlLmNvbSJ9.x-CIAk-CNiyQo2lvOviZcyCK-Bxy-P8CAOM4n8K4DZiYCbrtGAPc3RnFEbhDjLCNuLVWAj1HVVWGP" +
                "MSdOnMGBA");

        //get spark session
        SparkSession session = getSparkSession(defaultOptions);

        LocalSecretClient.LocalSecrets localSecrets = new LocalSecretClient.LocalSecrets();
        localSecrets.setSecretsForProject(project, new HashMap<String, String>() {{
                    put("aws-secret", "myPassword");
                }}
        );

        //create cortex session
        CortexSession cortexSession = getCortexSession(session, localSecrets);


        cortexSession.catalog().createDataSource(new DataSource(
                project,
                "myDataSource",
                null,
                null,
                SourceKind.batch,
                "member_id",
                Arrays.asList(""),
                new ResourceRef("connection"),
                "me"
        ));



        //get profileschema from the catalog
        ProfileSchema profileSchema = cortexSession.catalog().getProfileSchema(project, profileSchemaName);

        String stateCode = "KY";
        Function<Dataset<Row>, Dataset<Row>> filterState = (ds) -> ds.filter(ds.col("state_code").equalTo(stateCode));


        //build primary datasource
        IngestDataSourceJob ingestMemberBase = cortexSession.job().ingestDataSource(project, profileSchema.getPrimarySource().getName(), cortexSession.getContext());
        ingestMemberBase.formatDatasetForDataSource = filterState;
        ingestMemberBase.run();


        //build all joined datasources
        profileSchema.getJoins().forEach(join -> {
            IngestDataSourceJob ingestJoin = cortexSession.job().ingestDataSource(project, join.getName(), cortexSession.getContext());
            ingestJoin.run();
        });

        //build profile
        BuildProfileJob buildProfileJob = cortexSession.job().buildProfile(project, profileSchemaName, cortexSession.getContext());

        // when true skip feature catalog calculations
        if("true".equals(session.sqlContext().getConf(PROFILE_SKIP_FEATURE_CATALOG_ANALYTICS))) {
            buildProfileJob.performFeatureCatalogCalculations = () -> false;
        }

        // when true overwrite schema and partition on state_code -> zip_code
        if("true".equals(session.sqlContext().getConf(PROFILE_REFRESH_WITH_PARTITION))) {
            buildProfileJob.writerOptions = (dsw) -> dsw
                    .deltaMerge(null)
                    .option("overwriteSchema", "true")
                    .mode(SaveMode.Overwrite)
                    .partitionBy("state_code", "zip_code");
        }

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


        buildProfileJob.run();

/*
        try {
            Thread.sleep(3000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
*/
/*
        //bypass datasource
        Dataset<Row> memberBase = filterState.apply(
                cortexSession.read().connection(project,
                        cortexSession.catalog().getDataSource(project, profileSchema.getPrimarySource().getName()).getConnection().getName()
                ).load()
        );

        //....get additional datasets

        //...join

        cortexSession.write().profile(memberBase, project, profileSchemaName).partitionBy("state_code", "zip_code").option("overwriteSchema", "true").mode(SaveMode.Overwrite).save();
*/
    }
}
