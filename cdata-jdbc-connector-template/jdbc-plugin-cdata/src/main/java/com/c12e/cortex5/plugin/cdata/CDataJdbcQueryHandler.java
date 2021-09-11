/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
package com.c12e.cortex5.plugin.cdata;

import com.c12e.cortex5.PluginHelper;
import com.c12e.cortex5.RequestMapping;
import com.c12e.cortex5.plugin.JdbcRequestHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.*;

import static com.c12e.cortex5.JdbcRequestConstants.*;

/**
 * Cortex calls the default {@link RequestMapping} for all query requests.  Each request contains the connection properties
 * set during Cortex Connection creation.  In the default implementation the first request will initialize the connection pool.
 * The connection pool should be initialized in the SystemEventHandler implementation if these properties are known at system
 * startup.
 *
 * A thrown {@link Throwable} will be caught and returned to the caller with a 500 status.
 */
@RequestMapping
public class CDataJdbcQueryHandler implements JdbcRequestHandler {

    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    public static ObjectMapper mapper;
    String rtk = null;

    /**
     * Connection Request Handler Method
     * @param requestBody request body key/value pairs deserialized from JSON
     * @param helper      {@link PluginHelper}
     * @return JsonNode (Payload)
     * @throws Throwable
     */
    @Override
    public JsonNode handleRequest(Map<String, Object> requestBody, PluginHelper helper) throws Throwable {
        mapper = helper.getJsonObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        Map<String, Object> skillProperties = (Map) requestBody.get("properties");
        String connectionName = skillProperties.get("connectionName").toString();
        String connectionType = skillProperties.get("connectionType").toString();
        String apiEndpoint = requestBody.get("apiEndpoint").toString();
        String project = requestBody.get("projectId").toString();
        String token = requestBody.get("token").toString();
        helper.getLogger(this.getClass()).info("Fetching Connection Metadata for connection: {}", connectionName);
        JsonNode connMeta = getConnection(connectionName, apiEndpoint, project, token);
        if (connMeta != null && connMeta.has("params")) {
            ArrayNode params = (ArrayNode) connMeta.get("params");
            Map<String, String> connectionParamMap = new HashMap<>();
            for (JsonNode param : params) {
                connectionParamMap.put(param.get("name").textValue(), param.get("value").textValue());
            }
//            String connectionType = connMeta.get("connectionType").textValue();
            ObjectNode payload = mapper.createObjectNode();
            // Adding Request to Output Payload
            payload.set("request", mapper.valueToTree(requestBody));
            JsonNode resp;
            if (connectionType.equals("jdbc_generic")) {
                //Handle Generic JDBC Connection Query
                helper.getLogger(this.getClass()).info("Received call for generic jdbc connection");
                resp = handleJdbcRequest(requestBody, connectionParamMap, helper);
            } else {
                //Handle CDATA JDBC Connection Query
                helper.getLogger(this.getClass()).info("Received call for cdata jdbc connection");
                resp = handleCdataRequest(requestBody, connectionParamMap, helper);
            }
            // Adding Response to Output Payload
            payload.set("response", resp);
            response.set("payload", payload);
        }
        return response;
    }

    /**
     * Convert JsonNode to HashMap
     *
     * @param t Json Formatted String
     * @return Map Object
     */
    public Map<String, String> jsonToMap(String t) {
        HashMap<String, String> map = null;
        try {
            //convert JSON string to Map
            map = mapper.readValue(t, new TypeReference<HashMap<String, String>>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Handle CDATA JDBC Connection Query
     *
     * @param requestBody request body key/value pairs deserialized from JSON
     * @param connectionParamMap Cortex Connection Params key/value pairs
     * @param helper PluginHelper {@link PluginHelper}
     * @return Query Result as a JSON Array
     * @throws Exception
     */
    public JsonNode handleCdataRequest(Map<String, Object> requestBody, Map<String, String> connectionParamMap,
                                        PluginHelper helper) throws Exception {
        JsonNode response;
        synchronized (this) {
            if (Objects.isNull(helper.getConnectionManager(Driver.class))) {
                //Adds the jarfile location to classpath
                helper.getManagedContent(connectionParamMap.get(DRIVER));
                helper.setConnectionManager(ServiceLoader.load(Driver.class).iterator().next());
            }
        }
        rtk = connectionParamMap.get("run_time_key");
        Driver driver = helper.getConnectionManager(Driver.class);
        String driverClassName = driver.getClass().getName();
        Properties connectionProperties = getConnectionProperties(jsonToMap(connectionParamMap.get(PLUGIN_PROPERTIES)));
        helper.getLogger(this.getClass()).info("Received plugin properties: {}", connectionProperties);
        connectionProperties.setProperty("RTK", rtk);
        String[] classParts = StringUtils.split(driverClassName, ".");
        String connectionProtocol = classParts[1] + ":" + classParts[2] + ":";

        try (Connection conn = DriverManager.getConnection(connectionProtocol, connectionProperties);
             Statement st = conn.createStatement()) {
            response = executeQuery(requestBody, helper, st);
        }
        return response;
    }

    /**
     * Handle Generic JDBC Connection Query
     *
     * @param requestBody request body key/value pairs deserialized from JSON
     * @param connectionParamMap Cortex Connection Params key/value pairs
     * @param helper PluginHelper {@link PluginHelper}
     * @return Query Result as a JSON Array
     * @throws Exception
     */
    private JsonNode handleJdbcRequest(Map<String, Object> requestBody, Map<String, String> connectionParamMap,
                                       PluginHelper helper) throws Exception {
        JsonNode response = null;
        HikariDataSource dataSource;
        synchronized (this) {
            dataSource = helper.getConnectionManager(HikariDataSource.class);

            if (Objects.isNull(dataSource)) {
                if (Objects.nonNull(connectionParamMap.get(DRIVER))) {
                    helper.getManagedContent(connectionParamMap.get(DRIVER));
                }
                Driver d = helper.getDriver(connectionParamMap.get(DRIVER_CLASSNAME));
                DriverManager.registerDriver(d);

                HikariConfig hikariConfig = new HikariConfig();
                hikariConfig.setJdbcUrl(connectionParamMap.get(CONNECTION_URI));

                Object userName = connectionParamMap.get(CONNECTION_USER);
                Object password = connectionParamMap.get(CONNECTION_PASSWORD);
                if (Objects.nonNull(userName)) {
                    hikariConfig.setUsername(userName.toString());
                }
                if (Objects.nonNull(password)) {
                    hikariConfig.setPassword(password.toString());
                }

                dataSource = new HikariDataSource(hikariConfig);

                helper.setConnectionManager(dataSource);
            }
        }
        // TODO validate that there is a STATEMENT_QUERY*
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement()) {
            response = executeQuery(requestBody, helper, st);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Executes SQL/CDATA Query
     *
     * @param requestBody request body key/value pairs deserialized from JSON
     * @param helper PluginHelper {@link PluginHelper}
     * @param st Statement
     * @return Query Results as an Array
     * @throws Exception
     */
    private ArrayNode executeQuery(Map<String, Object> requestBody, PluginHelper helper, Statement st) throws Exception {
        Map<String, Object> payload = (Map) requestBody.get("payload");
        if (payload.containsKey(STATEMENT_QUERIES)) {
            return helper.runBatch(st, ((List<?>) payload.get(STATEMENT_QUERIES)).stream().map(Object::toString)
                    .collect(Collectors.toList()));
        } else {
            return helper.runQuery(st, payload.get(STATEMENT_QUERY).toString());
        }
    }

    /**
     * Get Cortex JDBC Connection Metadata
     *
     * @param connectionName Connection Name
     * @param apiEndpoint Cortex apiEndpoint
     * @param project Project Name
     * @param token JWT
     * @return Connection Metadata as JSON
     */
    public JsonNode getConnection(String connectionName, String apiEndpoint, String project, String token) {
        try {
            String url = String.format("%s/internal/projects/%s/connections/%s", apiEndpoint, project, connectionName);
            Request request = new Request.Builder().url(url)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
            Response res = HTTP_CLIENT.newCall(request).execute();
            assert res.body() != null;
            return mapper.readValue(res.body().string(), JsonNode.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Get Connection Properties from Plugin Properties for CDATA Connection
     *
     * @param pluginProperties Plugin properties from connection params
     * @return Connection Properties
     */
    protected Properties getConnectionProperties(Map<String, String> pluginProperties) {
        Properties properties = new Properties();
        pluginProperties.keySet().forEach(key -> properties.setProperty(key, pluginProperties.get(key)));
        return properties;
    }
}
