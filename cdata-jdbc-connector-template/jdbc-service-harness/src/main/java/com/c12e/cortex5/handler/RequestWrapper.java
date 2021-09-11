/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
package com.c12e.cortex5.handler;

import com.c12e.cortex5.PluginHelper;
import com.c12e.cortex5.plugin.JdbcRequestHandler;

import com.c12e.cortex5.utility.HarnessPluginHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.body.BodyHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.util.HashMap;
import java.util.Map;


/**
 * Wraps {@link JdbcRequestHandler} service call passing the unmarshalled request body and {@link PluginHelper} for request handling.
 */
public class RequestWrapper implements HttpHandler {

    private static final String TOKEN_KEY = "token";
    private static final String API_ENDPOINT = "apiEndpoint";
    protected HarnessPluginHelper helper;
    protected JdbcRequestHandler delegate;

    /**
     * Constructor
     *
     * @param delegate - the delegate request handler {@link JdbcRequestHandler}
     * @param helper   - the plugin helper {@link HarnessPluginHelper}
     */
    public RequestWrapper(JdbcRequestHandler delegate, HarnessPluginHelper helper) {
        this.delegate = delegate;
        this.helper = helper;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        Map<?, ?> requestBody = (Map<?, ?>) exchange.getAttachment(BodyHandler.REQUEST_BODY);
        Map<String, Object> convertedMap = new HashMap<>();
        requestBody.keySet().stream().map(Object::toString).forEach(key -> convertedMap.put(key, requestBody.get(key)));

        if (convertedMap.containsKey(TOKEN_KEY)) {
            helper.setJwt(convertedMap.get(TOKEN_KEY).toString());
        } else {
            helper.setJwt(exchange.getRequestHeaders().get("Authorization").get(0).replace("Bearer ", ""));
        }
        if (convertedMap.containsKey(API_ENDPOINT)) {
            helper.setApiEndpoint(convertedMap.get(API_ENDPOINT).toString());
        }
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseHeaders().put(Headers.SERVER, helper.getImplementationVersion());

        try {
            JsonNode responseJson = delegate.handleRequest(convertedMap, helper);
            exchange.getResponseSender().send(helper.getJsonObjectMapper().writeValueAsString(responseJson));
        } catch (Throwable e) {
            helper.getLogger(this.getClass()).error(e.toString(), e);
            exchange.setStatusCode(500);
            exchange.getResponseSender().send(e.toString());
        }
    }
}
