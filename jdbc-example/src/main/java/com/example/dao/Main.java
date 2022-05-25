package com.example.dao;

import com.c12e.fabric.ConfigurationProvider;
import com.c12e.fabric.CortexClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static spark.Spark.exception;
import static spark.Spark.initExceptionHandler;
import static spark.Spark.port;
import static spark.Spark.post;

public class Main {

    public static final Logger LOGGER = LoggerFactory.getLogger("jdbc-example");

    private static boolean allValuesPresent(String...params) {
        return Arrays.stream(params).filter(p -> Objects.isNull(p) || p.trim().isEmpty()).toArray().length > 0;
    }

    public static void main(String[] args) {
        port(5000); // default Cortex Fabric daemon Action port
        initExceptionHandler(Throwable::printStackTrace);

        Example example = new HikariExample();

        post("invoke", "application/json", (request, response) -> {
            try {
                // cortex payload parsing
                Map<String, Object> requestBody = ConfigurationProvider.mapper.readValue(request.body(), Map.class);
                String endpoint = (String) requestBody.get("apiEndpoint");
                String token = (String) requestBody.get("token");
                String project = (String) requestBody.get("projectId");
                Map<String, Object> payload = (Map<String, Object>) requestBody.get("payload");
                Map<String, Object> properties = (Map<String, Object>) requestBody.get("properties");
                String connectionName = (String) properties.get("connection_name");
                String sql = (String) payload.get("query");

                // validate params
                if (allValuesPresent(connectionName, sql)) {
                    throw new RuntimeException("'connection_name' skill property and 'query' in payload must be provided");
                }
                ConfigurationProvider configClient = new CortexClient(endpoint, token, project);
                ConfigurationProvider.JdbcConnectionParams jdbcParams = configClient.getConnectionParams(connectionName);
                // validate params
                LOGGER.info(String.format("Connection properties: %s", jdbcParams.getConnectionProperties().keySet()));
                List<String> errors = configClient.validateConnectionParams(jdbcParams);
                if (!errors.isEmpty()) {
                    errors.forEach(e -> LOGGER.error(e));
                    throw new Exception(String.format("Connection parameters validation failed. %s", errors));
                }
                // check any secure files like service account volume mapped
                try {
                    Files.list(Paths.get("/secure-storage")).forEach(f -> {
                        try {
                            LOGGER.info("File {} of size {} found in '/secure-storage'", f.getFileName(), Files.size(f));
                        } catch (IOException e) {
                            LOGGER.error("Failed to get file size of {} ", f.getFileName(), e);
                        }
                    });
                } catch (Exception e) {
                    // '/secure-storage' volume mount is optional, so may not be present. So, just logging the error
                    LOGGER.error("Failed to list '/secure-storage' volume mount", e);
                }

                // setup on validation success
                example.setup(jdbcParams);
                return example.execute(sql);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        });

        exception(Exception.class, (e, request, response) -> {
            response.status(500);
            response.body(e.getMessage());
        });
    }
}
