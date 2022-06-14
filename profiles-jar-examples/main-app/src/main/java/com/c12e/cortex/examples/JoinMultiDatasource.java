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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@CommandLine.Command(name = "sql-join-conns", description = "Example Joining Connections", mixinStandardHelpOptions = true)
public class JoinMultiDatasource extends  BaseCommand implements Runnable {
    @CommandLine.Option(names = {"-p", "--project"}, description = "Project to use", required = true)
    String project;

    @CommandLine.Option(names = {"-c", "--config"}, description = "Ingestion config file path", required = true)
    String configFilePath;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec cmdSpec;

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Code to run for a command
     */
    @Override
    public void run() {
        //get spark session
        SparkSession session = getSparkSession(getDefaultProps());
        session.conf().getAll().toStream().print();

        //create fabric session
        LocalSecretClient.LocalSecrets localSecrets = new LocalSecretClient.LocalSecrets();
        FabricSession fabricSession = getFabricSession(session, localSecrets);

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
        String queryTemplate = (String) ((Map)config.get("join")).get("query");
        //load the two connections from fabric through the api-server
        List<Map> connections = (List<Map>) config.get("connections");
        for (Map connection : connections) {
            String name = (String) connection.get("name");
            Integer limit = (Integer) connection.get("limit");
            String[] select = (String[]) connection.get("select");
            String filter = (String) connection.get("filter");

            Dataset<Row> ds = fabricSession.read().readConnection(project, name).load();
            if (!Objects.isNull(select) && select.length > 0) ds = ds.selectExpr(select);
            if (!Objects.isNull(filter) && filter.length() > 0) ds = ds.filter(filter);
            if (!Objects.isNull(limit) && limit > 0) ds = ds.limit(limit);

            ds.createOrReplaceTempView(name);
        }
        long start = System.currentTimeMillis();
        Dataset<Row> ds = session.sql(queryTemplate);
        System.out.println("Join performed in " + (System.currentTimeMillis() - start));

        // *** Handle duplicate columns after join ***
        // drop duplicate column names (update this as per use case. if duplicate column name are not duplicates, rename instead of drop)
        List<String> duplicates = Arrays.stream(ds.columns()).collect(Collectors.groupingBy(Function.identity()))
                .entrySet()
                .stream()
                .filter(e -> e.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        // mark repeated column for drop
        List<String> newColumns = new ArrayList<>();
        for (String col : ds.columns()) {
            // if repeated and added in new columns then mark for drop
            if (duplicates.contains(col) && newColumns.contains(col)) {
                newColumns.add(col + "_delete");
            } else {
                newColumns.add(col);
            }
        }
        // drop columns marked for drop
        ds = ds.toDF(newColumns.toArray(new String[0]))
                .drop(duplicates.stream().map(c -> c + "_delete").collect(Collectors.toList()).toArray(new String[0]));
        System.out.println("Drop repeated columns performed in " + (System.currentTimeMillis() - start));

        String output = (String) ((Map) config.get("output")).get("connection");
        fabricSession.write().writeConnection(ds, project, output).mode(SaveMode.Overwrite).save();

        Dataset<Row> dataSourceDs = fabricSession.read().readConnection(project, output).load();
        System.out.println("Saved DS: " + dataSourceDs.count());
    }
}
