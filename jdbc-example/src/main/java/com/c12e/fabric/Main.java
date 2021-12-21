package com.c12e.fabric;

import com.c12e.fabric.jdbc.FabricJdbcStatement;
import com.example.dao.ConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static spark.Spark.initExceptionHandler;
import static spark.Spark.port;
import static spark.Spark.post;

public class Main {

    private static boolean allValuesPresent(String...params) {
        return Arrays.stream(params).filter(p -> Objects.isNull(p) || p.trim().isEmpty()).toArray().length > 0;
    }

    public static void main(String[] args) {
        port(5000); // default Cortex Fabric daemon Action port
        initExceptionHandler((e) -> e.printStackTrace());

        ConnectionManager connectionManager = new ConnectionManager();
        post("invoke", "application/json", (request, response) -> {
            try {
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

                if (!connectionName.equals(connectionManager.getConnectionPoolName())) { // if not initialized or connection name changed in skill properties
                    ConfigurationProvider configClient = new CortexClient(endpoint, token, project, "lib");
                    ConfigurationProvider.JdbcConnectionParams jdbcParams = configClient.getConnectionParams(connectionName);
                    connectionManager.initializeHikariPool(jdbcParams);
                }

                try (Connection connection = connectionManager.getHikariPoolConnection();
                     Statement statement = new FabricJdbcStatement(connection.createStatement());
                     ResultSet resultSet = statement.executeQuery(sql)) {
                    return connectionManager.convertToJSON(resultSet);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        });
    }
}
