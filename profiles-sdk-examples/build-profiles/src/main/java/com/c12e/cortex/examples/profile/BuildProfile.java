
package com.c12e.cortex.examples.profile;

import com.c12e.cortex.examples.local.SessionExample;
import com.c12e.cortex.phoenix.ProfileSchema;
import com.c12e.cortex.profiles.CortexSession;
import com.c12e.cortex.profiles.module.job.BuildProfileJob;
import com.c12e.cortex.profiles.module.job.IngestDataSourceJob;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Example CLI application that builds Profiles for the specified ProfileSchema.
 */
@Command(name = "build-profile", description = "Example Profile Build", mixinStandardHelpOptions = true)
public class BuildProfile implements Runnable {
    @Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @Option(names = {"-ps", "--profile-schema"}, description = "ProfileSchema Name", required = true)
    String profileSchemaName;

    @Override
    public void run() {
        // create the cortex session
        SessionExample example = new SessionExample();
        CortexSession cortexSession = example.getCortexSession();
        buildProfiles(cortexSession, project, profileSchemaName);
    }

    public void buildProfiles(CortexSession cortexSession,
                              String project,
                              String profileSchemaName) {
        // get ProfileSchema from the catalog
        ProfileSchema profileSchema = cortexSession.catalog().getProfileSchema(project, profileSchemaName);

        // Build the Primary DataSource.
        // Options can be set to control the job. The FeatureCatalog calculations are performed to infer the features in
        // the datasource, it can optionally be disabled.
        IngestDataSourceJob ingestMemberBase = cortexSession.job().ingestDataSource(project, profileSchema.getPrimarySource().getName(), cortexSession.getContext());
        ingestMemberBase.performFeatureCatalogCalculations = () -> true;
        ingestMemberBase.run();

        // Build all joined DataSources
        profileSchema.getJoins().forEach(join -> {
            IngestDataSourceJob ingestJoin = cortexSession.job().ingestDataSource(project, join.getName(), cortexSession.getContext());
            ingestJoin.performFeatureCatalogCalculations = () -> true;
            ingestJoin.run();
        });

        // Build Profiles
        BuildProfileJob buildProfileJob = cortexSession.job().buildProfile(project, profileSchemaName, cortexSession.getContext());
        buildProfileJob.performFeatureCatalogCalculations = () -> true;
        buildProfileJob.run();
    }
}


