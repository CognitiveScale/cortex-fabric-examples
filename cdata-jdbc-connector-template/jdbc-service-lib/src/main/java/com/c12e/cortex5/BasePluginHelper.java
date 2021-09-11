/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
package com.c12e.cortex5;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

/**
 * Base implementation of {@link PluginHelper}.  Extensions in plugins will not be used.
 */
public abstract class BasePluginHelper implements PluginHelper {

    Object connectionManager;
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public abstract File getManagedContent(String path);

    @Override
    public Driver getDriver(String className) {
        try {
            return Driver.class.cast(ClassLoader.getSystemClassLoader().loadClass(className).newInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ArrayNode runBatch(Statement st, List<String> queries) throws Exception {
        for (String q : queries) {
            st.addBatch(q);
        }
        ArrayNode arrayNode = objectMapper.createArrayNode();

        int[] in = st.executeBatch();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("OperationsPerformed", in.length);
        arrayNode.add(node);
        return arrayNode;
    }

    @Override
    public ArrayNode runQuery(Statement st, String query) throws Exception {
        if (query.toLowerCase().contains("select")) {
            return convertToJSON(st.executeQuery(query));
        } else {
            ArrayNode arrayNode = objectMapper.createArrayNode();

            int rowsModified = st.executeUpdate(query);
            ObjectNode node = objectMapper.createObjectNode();
            node.put("RowsModified", rowsModified);
            arrayNode.add(node);
            return arrayNode;
        }
    }

    protected ArrayNode convertToJSON(ResultSet resultSet)
            throws Exception {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        while (resultSet.next()) {
            int total_column_count = resultSet.getMetaData().getColumnCount();
            ObjectNode objectNode = objectMapper.createObjectNode();
            for (int i = 1; i <= total_column_count; i++) {
                String column_name = resultSet.getMetaData().getColumnName(i);
                Object value = resultSet.getObject(column_name);
                if(Objects.isNull(value)){
                    value = "null";
                }
                objectNode.put(column_name, value.toString());
            }
            arrayNode.add(objectNode);
        }
        return arrayNode;
    }

    @Override
    public <T> T getConnectionManager(Class<T> managerType) {
        return managerType.cast(this.connectionManager);
    }

    @Override
    public <T> void setConnectionManager(T manager) {
        this.connectionManager = manager;
    }

    @Override
    public ObjectMapper getJsonObjectMapper() {
        return objectMapper;
    }

    @Override
    public Logger getLogger(Class<?> logger) {
        return LoggerFactory.getLogger(logger);
    }
}
