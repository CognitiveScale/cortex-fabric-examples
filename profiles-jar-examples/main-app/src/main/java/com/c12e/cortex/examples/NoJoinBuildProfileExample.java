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
import com.c12e.cortex.profiles.module.job.BuildProfileJob;
import com.c12e.cortex.profiles.module.job.IngestDataSourceJob;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.sql.SparkSession;
import picocli.CommandLine;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

@CommandLine.Command(name = "no-join-example", description = "Example Building Profiles", mixinStandardHelpOptions = true)
public class NoJoinBuildProfileExample extends  BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @CommandLine.Option(names = {"-c", "--config"}, description = "Ingestion config file path", required = true)
    String configFilePath;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec cmdSpec;

    private static final ObjectMapper mapper = new ObjectMapper();
    private final String checkpointFileName = "checkpoint.yml";

    private Boolean isProcessed(String checkpointDir) {
        System.out.println("Checking for processed batch...");
        Configuration conf = new Configuration();
        try {
            FileSystem hdfs = FileSystem.get(new URI(checkpointDir), conf);
            Path file = new Path(checkpointDir + "/" + checkpointFileName);
            return hdfs.exists(file);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void writeCheckpoint(String checkpointDir) {
        System.out.println("Writing checkpoint...");
        Configuration conf = new Configuration();
        try {
            FileSystem hdfs = FileSystem.get(new URI(checkpointDir), conf);
            Path file = new Path(checkpointDir + "/" + checkpointFileName);
            if (hdfs.exists(file)) {
                throw new RuntimeException("Checkpoint filee already exists, shouldn't have continued in the first place.");
            }
            OutputStream os = hdfs.create(file);
            BufferedWriter br = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            br.write("processed: true");
            br.close();
            hdfs.close();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Code to run for a command
     */
    @Override
    public void run() {
        System.out.println(new Date());
        //get spark session
        SparkSession session = getSparkSession(getDefaultProps());
        session.conf().getAll().toStream().print();

        //create fabric session
        LocalSecretClient.LocalSecrets localSecrets = new LocalSecretClient.LocalSecrets();
        CortexSession cortexSession = getCortexSession(session, localSecrets);

        HashMap config;
        try {
            JsonNode json = mapper.readTree(Paths.get(configFilePath).toFile());
            config = mapper.convertValue(json, HashMap.class);
            ((Map)config.getOrDefault("variables", Collections.emptyMap())).forEach((k, v) -> session.sql(String.format("set %s=%s", k, v)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("Ingestion confile file %s must be valid JSON file", configFilePath), e);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Config file path %s not found", configFilePath), e);
        }

        List<String> batchDirs = (List<String>) config.get("batchDirList");
        List<Map> profiles = (List<Map>) config.get("profiles");

        for (String batchDir: batchDirs) {
            // Skip processing if already processed.
            if (this.isProcessed(batchDir)) {
                System.out.println(String.format("This batch is already processed, skipping...", batchDir));
                continue;
            }

            System.out.println("Batch not procssed, continuing...");

            // Iterate over profiles in config
            for (Map profile : profiles) {
                List<Map> dataSources = (List<Map>) profile.get("dataSources");

                for (Map dataSource: dataSources) {
                    // Build primary data source
                    String dataSourceName = (String) dataSource.get("name");
                    String filterExpr = (String) dataSource.get("filter");
                    Integer limit = (Integer) dataSource.get("limit");
                    String profileSchemaName = (String) dataSource.get("profileSchemaName");

                    // Ingest data source
                    IngestDataSourceJob ingestDataSourceJob = cortexSession.job().ingestDataSource(project, dataSourceName, cortexSession.getContext());
                    ingestDataSourceJob.performFeatureCatalogCalculations = () -> true;
                    ingestDataSourceJob.formatDatasetForDataSource = (ds) -> ds.filter(filterExpr);
                    ingestDataSourceJob.run();

                    // Build profile
                    BuildProfileJob buildProfileJob = cortexSession.job().buildProfile(project, profileSchemaName, cortexSession.getContext());
                    buildProfileJob.performFeatureCatalogCalculations = () -> true;
                    buildProfileJob.run();
                }

            // DeltaMerge everyday ??
            }
            // Write checkpoint
            writeCheckpoint(batchDir);
        }
    }
}
