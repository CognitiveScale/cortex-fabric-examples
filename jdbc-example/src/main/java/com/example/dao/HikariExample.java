package com.example.dao;

import com.c12e.fabric.ConfigurationProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class HikariExample implements Example {

    private HikariDataSource connectionPool;

    @Override
    public void setup(ConfigurationProvider.JdbcConnectionParams connectionParams) {
        if (!Objects.isNull(connectionPool)) {
            connectionPool.close();
        }
        connectionPool = createHikariDataSource(connectionParams);
    }

    @Override
    public JsonNode execute(String sql) throws SQLException {
        if (Objects.isNull(connectionPool)) {
            throw new SQLException("Connection pool is not initialized. Please call setup to initialize the pool");
        }
        try(Connection connection =  connectionPool.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
        ) {
            return convertToJSON(resultSet);
        }
    }

    private HikariDataSource createHikariDataSource(ConfigurationProvider.JdbcConnectionParams connectionParams) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(connectionParams.getDriverClassname());
        config.setDataSourceProperties(connectionParams.getConnectionProperties());
        config.setJdbcUrl(connectionParams.getConnectionProtocol());
        if (connectionParams.getConnectionProperties().getProperty("username") != null) config.setUsername(connectionParams.getConnectionProperties().getProperty("username"));
        return new HikariDataSource(config);
    }
}
