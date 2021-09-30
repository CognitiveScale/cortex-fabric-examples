/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
package com.c12e.cortex5.plugin;

import com.c12e.cortex5.HttpRequestType;
import com.c12e.cortex5.JdbcRequestConstants;
import com.c12e.cortex5.PluginHelper;
import com.c12e.cortex5.RequestMapping;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.Map;

/**
 * Request handler with requestBody containing {@link JdbcRequestConstants} and a {@link PluginHelper} utility.
 * Relative path and {@link HttpRequestType} are declared through the {@link RequestMapping} annotation which should be present on every implementation of this interface.
 */
public interface JdbcRequestHandler {
    /**
     * Called from servlet dispatcher at the annotated {@link RequestMapping}
     *
     * @param requestBody request body key/value pairs deserialized from JSON
     * @param helper {@link PluginHelper}
     * @return {@link ArrayNode} to be serialized
     * @throws Throwable throws exception if request fails
     */
    JsonNode handleRequest(Map<String, Object> requestBody, PluginHelper helper) throws Throwable;
}
