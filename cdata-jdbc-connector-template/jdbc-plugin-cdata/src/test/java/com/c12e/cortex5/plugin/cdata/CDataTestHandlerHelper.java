/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
package com.c12e.cortex5.plugin.cdata;

import com.c12e.cortex5.BasePluginHelper;
import com.c12e.cortex5.PluginHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.sql.Statement;
import java.util.Objects;

public class CDataTestHandlerHelper extends BasePluginHelper implements PluginHelper {
    JsonNode managedContent;

    @Override
    public File getManagedContent(String key) {
        if (Objects.isNull(managedContent)) {
            managedContent = getResourceAsJson("/managedContent.json");
        }
        System.out.println(managedContent);
        System.out.println(key);
        return new File(getClass().getResource(managedContent.get(key).asText()).getFile());
    }

    @Override
    public ArrayNode runQuery(Statement st, String query) throws Exception {
        JsonNode data = getJsonObjectMapper().readTree(new File(getClass().getResource("/data.json").getFile()));
        System.out.println(data);
        return (ArrayNode) data.get("people");
    }

    protected JsonNode getResourceAsJson(String resourceLocation) {
        try {
            return getJsonObjectMapper().readTree(new File(getClass().getResource(resourceLocation).getFile()));
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
