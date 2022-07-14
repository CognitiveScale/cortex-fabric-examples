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
import com.c12e.cortex.profiles.module.CortexDeltaMergeBuilder;
import com.c12e.cortex.profiles.module.job.BuildProfileJob;
import com.c12e.cortex.profiles.module.job.IngestDataSourceJob;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.delta.tables.DeltaTable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.HashPartitioner;
import org.apache.spark.Partitioner;
import org.apache.spark.sql.*;
import picocli.CommandLine;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@CommandLine.Command(name = "partition", description = "Example Building Profiles", mixinStandardHelpOptions = true)
public class Partition extends  BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @CommandLine.Option(names = {"-c", "--config"}, description = "Ingestion config file path", required = true)
    String configFilePath;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec cmdSpec;

    private static final ObjectMapper mapper = new ObjectMapper();

    public class CustomPartition extends Partitioner {

        private final int numParts;
        public CustomPartition(int i) {
            numParts = i;
        }
        @Override
        public int numPartitions() {
            return numParts;
        }

        // Applying the logic to decide the target partition number.
        @Override
        public int getPartition(Object key) {
            return ((String) key).charAt(0) % numParts;
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

            System.out.println("Batch not procssed, continuing...");

            // Iterate over profiles in config
            for (Map profile : profiles) {
                List<Map> dataSources = (List<Map>) profile.get("dataSources");

                for (Map dataSource: dataSources) {
                    // Build primary data source
                    String dataSourceName = (String) dataSource.get("name");

                    System.out.println("Ingesting Processing DataSource: " + dataSourceName);
                    System.out.println("Building profile for DataSource: " + dataSourceName);

                    /**
                     * simply broadcast
                     *

                    DeltaTable existing =  cortexSession.read().dataSource(project, dataSourceName).load();
                    Dataset<Row> incoming = cortexSession.read().connection(project, dataSourceName).load();

                    existing.as("existing")
                            .merge(functions.broadcast(incoming.as("incoming")), String.format("existing.%s = incoming.%s", dataSource.get("primaryKey"), dataSource.get("primaryKey")))
                            .whenMatched().updateAll()
                            .whenNotMatched().insertAll()
                            .execute();
                     */


                    /**
                     * The initial load build with partitions
                     */

                    Dataset<Row> ds = cortexSession.read().connection(project, dataSourceName).load();
                    // we want partitions to be 1gb atleast
                    // and partitions inside partitions to be 30MB
                    ds = ds.withColumn("par", functions.hash(ds.col((String) dataSource.get("primaryKey"))).mod(448));

                    cortexSession
                            .write()
                            .dataSource(ds, project, dataSourceName)
                            .deltaMerge(CortexDeltaMergeBuilder.getInstance())
                            .partitionBy("par")
                            .save();




                    /**
                     * Iterates over the partitions and merges only one partition at a time of the incoming data
                     */



                     // Ingest data source
//                     Dataset<Row> ds = cortexSession.read().connection(project, dataSourceName).load();
//                     ds = ds.withColumn("par", functions.hash(ds.col((String) dataSource.get("primaryKey"))).mod(9));
//                     CortexDeltaMergeBuilder mergeBuilder = CortexDeltaMergeBuilder.getInstance();
//                     List<Integer> partitions = ds.select("par").distinct().collectAsList().stream().map(row -> row.getInt(0)).collect(Collectors.toList());
//
//
//                     ds.persist();
//                     // try 5 partitions at the same time
//
//                    for(int i = 0; i < partitions.size() - 5; i += 5) {
//                        List<Integer> parts = partitions.subList(i, i+5);
//                        String partitionsList = parts.stream().map(String::valueOf).collect(Collectors.joining(","));
////                        Dataset<Row> incoming = ds.filter(ds.col("par").isin(parts));
//
//                        mergeBuilder.onCondition = (builder) ->
//                                String.format("existing.par IN (%s) AND existing.%s = incoming.%s",
//                                        partitionsList, builder.targetPrimaryKey, builder.sourcePrimaryKey);
//                        mergeBuilder.merge = (builder) -> {
//                            return builder.deltaTable.alias(builder.existing)
//                                    .merge(
//                                            functions.broadcast(builder.ds.filter(builder.ds.col("par").isin(parts.toArray())).as(builder.incoming)),
//                                            builder.onCondition.apply(builder)
//                                    );
//                        };
//                        cortexSession
//                                .write()
//                                .dataSource(ds, project, dataSourceName)
//                                .deltaMerge(mergeBuilder)
//                                .partitionBy("par")
//                                .save();
//
//                    }


//                    for (Integer part : partitions) {
//
//                        String partitionsList = partitions.stream().map(String::valueOf).collect(Collectors.joining(","));
//
//
//                        // try with SQL
//                        incoming = ds.filter(builder.ds.col("par").equalTo(part).persist()
//
//
//                        mergeBuilder.onCondition = (builder) ->
//                                String.format("existing.par = %s AND existing.%s = incoming.%s",
//                                        part, builder.targetPrimaryKey, builder.sourcePrimaryKey);
//                        mergeBuilder.merge = (builder) -> {
//                            return builder.deltaTable.alias(builder.existing)
//                                    .merge(
//                                            functions.broadcast(builder.ds.as(builder.incoming)),
//                                            builder.onCondition.apply(builder)
//                                    );
//                        };
//                        cortexSession
//                                .write()
//                                .dataSource(incoming, project, dataSourceName)
//                                .deltaMerge(mergeBuilder)
//                                .partitionBy("par")
//                                .save();
//                        incoming.unpersist();
//                    }
//                     ds.unpersist();

                    /**
                     * iterate over partitions and broadcast without SDK
                     *


                    DeltaTable existing =  cortexSession.read().dataSource(project, dataSourceName).load();
                    Dataset<Row> incoming = cortexSession.read().connection(project, dataSourceName).load();
                    incoming = incoming.withColumn("par", functions.hash(incoming.col((String) dataSource.get("primaryKey"))).mod(100));
                    List<Integer> partitions = incoming.select("par").distinct().collectAsList().stream().map(row -> row.getInt(0)).collect(Collectors.toList());

                    incoming.persist();
                    incoming.count();

                    for (Integer part : partitions) {

                        existing.as("existing")
                                .merge(functions.broadcast(incoming.filter(incoming.col("par").equalTo(part)).as("incoming")), String.format("existing.par = %s AND existing.%s = incoming.%s", part, dataSource.get("primaryKey"), dataSource.get("primaryKey")))
                                .whenMatched().updateAll()
                                .whenNotMatched().insertAll()
                                .execute();
                    }
                     */
                }
            }
        }
    }
}

