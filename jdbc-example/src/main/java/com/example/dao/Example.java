package com.example.dao;

import com.c12e.fabric.ConfigurationProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.c12e.fabric.ConfigurationProvider.mapper;

public interface Example {

    void setup(ConfigurationProvider.JdbcConnectionParams params) throws SQLException, IOException;

    JsonNode execute(String query) throws SQLException, IOException;

    default JsonNode convertToJSON(ResultSet resultSet) throws SQLException {
        ArrayNode arrayNode = mapper.createArrayNode();
        while (resultSet.next()) {
            int columnCount = resultSet.getMetaData().getColumnCount();
            ObjectNode objectNode = mapper.createObjectNode();
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
        ObjectNode payload = mapper.createObjectNode();
        ObjectNode result = mapper.createObjectNode();
        result.put("result", arrayNode);
        payload.put("payload", result);
        return payload;
    }

    default JsonNode convertToJSON(List<? extends Object> resultSet) throws SQLException, IOException {
        return mapper.readTree(mapper.writeValueAsString(Collections.singletonMap("payload", Collections.singletonMap("result", resultSet))));
    }
}
