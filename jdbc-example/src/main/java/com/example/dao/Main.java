package com.example.dao;

import com.c12e.fabric.ConfigurationProvider;
import com.c12e.fabric.CortexClient;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static spark.Spark.exception;
import static spark.Spark.initExceptionHandler;
import static spark.Spark.port;
import static spark.Spark.post;

public class Main {

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
