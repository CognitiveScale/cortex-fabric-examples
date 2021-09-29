/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
package com.c12e.cortex5;

import com.c12e.cortex5.plugin.JdbcRequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;

import java.io.File;
import java.sql.Driver;
import java.sql.Statement;
import java.util.List;

/**
 * Implemented in the Cortex JDBC Connector Framework.  Method signatures should not be changed.
 */
public interface PluginHelper {

    /**
     * From tenant S3 bucket
     *
     * @param path path to S3 content
     * @return local content location
     */
    File getManagedContent(String path);

    /**
     * Creates a {@link Driver} instance of specified className.
     *
     * @param className FQCN of the database driver
     * @return {@link Driver} casted instance
     */
    Driver getDriver(String className);

    /**
     *
     * @param st the statement to use for querying
     * @param queries the queries to run as a list of SQL statements
     * @return {@link ArrayNode} containing the query results
     * @throws Exception throws exception if querying fails
     */
    ArrayNode runBatch(Statement st, List<String> queries) throws Exception;

    /**
     *
     * @param st the statement to use for querying
     * @param query the query as SQL
     * @return {@link ArrayNode} containing the query results
     * @throws Exception throws exception if querying fails
     */
    ArrayNode runQuery(Statement st, String query) throws Exception;

    /**
     * Retrieve the connection manager as type, the type should be coordinated throughout the plugin.
     * @param managerType the connection manager class type to be retrieved from the helper
     * @param <T> the connection manager type
     * @return the connection manager as type T
     */
    <T> T getConnectionManager(Class<T> managerType);

    /**
     * Saved on the helper to be passed into the {@link JdbcRequestHandler} during each request
     * @param manager the connection manager to be set on the helper
     * @param <T> the connection manager type
     */
    <T> void setConnectionManager(T manager);

    /**
     * The object mapper used for JSON marshalling
     * @return {@link ObjectMapper}
     */
    ObjectMapper getJsonObjectMapper();

    /**
     * Retrieve the provided logger
     * @param loggedClass class to be logged
     * @return {@link Logger} class specific logger
     */
    Logger getLogger(Class<?> loggedClass);
}
