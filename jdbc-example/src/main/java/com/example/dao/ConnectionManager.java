package com.example.dao;

import com.c12e.fabric.ConfigurationProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.Objects;

public class ConnectionManager {

    private Entry<String, HikariDataSource> connectionPool = null;

    public String getConnectionPoolName() {
        return Objects.isNull(connectionPool) ? null : connectionPool.getKey();
    }

    public void initializeHikariPool(ConfigurationProvider.JdbcConnectionParams connectionParams) {
        if (Objects.isNull(connectionPool) || !connectionPool.getKey().equals(connectionParams.getConnectionName())) {
            if (!Objects.isNull(connectionPool)) {
                connectionPool.getValue().close();
            }
            HikariDataSource ds = createHikariDataSource(connectionParams);
            connectionPool = new AbstractMap.SimpleEntry<>(connectionParams.getConnectionName(), ds);
        }
    }

    public Connection getHikariPoolConnection() throws SQLException {
        if (Objects.isNull(connectionPool)) {
            throw new SQLException("Connection pool is not initialized. Please use initializeHikariPool to initialize the pool before using it");
        }
        return connectionPool.getValue().getConnection();
    }

    private HikariDataSource createHikariDataSource(ConfigurationProvider.JdbcConnectionParams connectionParams) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(connectionParams.getDriverClassname());
        config.setDataSourceProperties(connectionParams.getConnectionProperties());
        config.setJdbcUrl(connectionParams.getConnectionProtocol());

        return new HikariDataSource(config);
    }

    public ObjectNode convertToJSON(ResultSet resultSet)
            throws Exception {
        ArrayNode arrayNode = ConfigurationProvider.mapper.createArrayNode();
        while (resultSet.next()) {
            int columnCount = resultSet.getMetaData().getColumnCount();
            ObjectNode objectNode = ConfigurationProvider.mapper.createObjectNode();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = resultSet.getMetaData().getColumnName(i);
                Object value = resultSet.getObject(columnName);
                if(Objects.isNull(value)){
                    value = "null";
                }
                objectNode.put(columnName, value.toString());
            }
            arrayNode.add(objectNode);
        }
        ObjectNode payload = ConfigurationProvider.mapper.createObjectNode();
        ObjectNode result = ConfigurationProvider.mapper.createObjectNode();
        result.put("result", arrayNode);
        payload.put("payload", result);
        return payload;
    }
}
