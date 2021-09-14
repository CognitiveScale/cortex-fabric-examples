/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
package com.c12e.cortex5.plugin.cdata.test;

import com.c12e.cortex5.JdbcRequestConstants;
import com.c12e.cortex5.plugin.cdata.CDataTestHandlerHelper;
import com.c12e.cortex5.plugin.cdata.CDataJdbcQueryHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class CDataJdbcRequestHandlerTest {
    private CDataTestHandlerHelper helper = new CDataTestHandlerHelper();

    @InjectMocks
    @Spy
    CDataJdbcQueryHandler queryHandlerMock = new CDataJdbcQueryHandler();

    @BeforeEach
    public void setup() {
        //if we don't call below, we will get NullPointerException
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConnection() throws Throwable {

        ObjectNode node = (ObjectNode) helper.getJsonObjectMapper().readTree(new File(getClass().getResource("/connection.json").getFile()));
        Mockito.doReturn(node).when(queryHandlerMock).getConnection("test", "api", "project", "token");

        JsonNode reqNode = getResourceAsJson("/testRequest.json");

        Map<String, Object> map = helper.getJsonObjectMapper().convertValue(reqNode, new TypeReference<HashMap<String, Object>>() {
        });
        JsonNode response = queryHandlerMock.handleRequest(map, helper);
        Assert.assertEquals(20, response.get("payload").get("response").get(0).get("personal").get("age").intValue());
    }

    @Test
    public void testQuery() throws Throwable {
        ObjectNode node = (ObjectNode) helper.getJsonObjectMapper().readTree(new File(getClass().getResource("/connection.json").getFile()));
        Mockito.doReturn(node).when(queryHandlerMock).getConnection("test", "api", "project", "token");

        JsonNode reqNode = getResourceAsJson("/testRequest.json");
        Map<String, Object> map = helper.getJsonObjectMapper().convertValue(reqNode, new TypeReference<HashMap<String, Object>>() {
        });

//        ObjectNode payload = helper.getJsonObjectMapper().createObjectNode();
//        payload.put(JdbcRequestConstants.STATEMENT_QUERY,  "SELECT [personal.name.last], [personal.name.first], [vehicles.1.type], [vehicles.1.model] FROM people WHERE [personal.name.last] = 'Roberts' AND [personal.name.first] = 'Jane'");
//        map.put("payload", (JsonNode) payload);

        JsonNode response = queryHandlerMock.handleRequest(map, helper);

        Assert.assertEquals("Doe", response.get("payload").get("response").get(0).get("personal").get("name").get("last").asText());
    }

    protected JsonNode getResourceAsJson(String resourceLocation) {
        try {
            ObjectNode node = (ObjectNode) helper.getJsonObjectMapper().readTree(new File(getClass().getResource(resourceLocation).getFile()));
            String pluginProperties = node.get("plugin_properties").asText();
            String dataLocation = this.getClass().getResource("/data.json").toString();
            node.put("plugin_properties", StringUtils.replace(pluginProperties, "${data.json}", dataLocation));
            return node;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
