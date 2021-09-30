/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
package com.c12e.cortex5.loader;

import com.c12e.cortex5.HttpRequestType;
import com.c12e.cortex5.PluginHelper;
import com.c12e.cortex5.RequestMapping;
import com.c12e.cortex5.plugin.JdbcRequestHandler;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Loads all {@link JdbcRequestHandler} creating a mapping to a service URL through the {@link RequestMapping}.
 */
public class RequestHandlerLoader {

    PluginHelper pluginHelper;

    /**
     * Constructs a {@link RequestHandlerLoader}
     *
     * @param pluginHelper the plugin helper instance
     */
    public RequestHandlerLoader(PluginHelper pluginHelper) {
        this.pluginHelper = pluginHelper;
    }

    /**
     * Validates and maps all {@link JdbcRequestHandler} found in the plugin.  Ensures there are no duplicates and that the default mapping ("/invoke", {@link HttpRequestType} POST) is present.
     *
     * @param pluginClassloader plugin classloader
     * @return {@link Map} of {@link RequestMapping} to {@link JdbcRequestHandler}
     */
    public Map<RequestMapping, JdbcRequestHandler> getValidRequestHandlers(ClassLoader pluginClassloader) {
        List<JdbcRequestHandler> requestHandlers = getRequestHandlers(pluginClassloader);
        HashMap<RequestMapping, JdbcRequestHandler> requestHandlerMap = new HashMap<>();
        System.out.println(requestHandlers);

        Boolean isDefaultFound = false;
        try {
            String defaultPath = (String) RequestMapping.class.getDeclaredMethod("path").getDefaultValue();
            HttpRequestType defaultMapping = (HttpRequestType) RequestMapping.class.getDeclaredMethod("requestType").getDefaultValue();

            for (JdbcRequestHandler requestHandler : requestHandlers) {
                RequestMapping requestMapping = requestHandler.getClass().getAnnotation(RequestMapping.class);
                System.out.println(requestMapping.path());
                System.out.println(defaultPath);
                if (!isDefaultFound) {
                    isDefaultFound = StringUtils.equals(requestMapping.path(), defaultPath) && requestMapping.requestType() == defaultMapping;
                }

                if (requestHandlerMap.containsKey(requestMapping)) {
                    pluginHelper.getLogger(this.getClass()).warn("Throwing out duplicate request mapping: " + requestMapping.path() + " method: " + requestMapping.requestType());
                } else {
                    requestHandlerMap.put(requestMapping, requestHandler);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!isDefaultFound) {
            System.out.println(requestHandlerMap);
            throw new RuntimeException("No default request mapping found");
        }
        return requestHandlerMap;
    }

    protected List<JdbcRequestHandler> getRequestHandlers(ClassLoader pluginClassloader) {
        Reflections reflections = new Reflections(pluginClassloader);
        Set<Class<?>> requestMappings = reflections.getTypesAnnotatedWith(RequestMapping.class);
        System.out.println(requestMappings);
        return requestMappings.stream().map(type -> {
            try {
                Object typeInstance = type.getDeclaredConstructor().newInstance();
                return (JdbcRequestHandler) typeInstance;
            } catch (Exception e) {
                pluginHelper.getLogger(this.getClass()).warn("Unable to instantiate JdbcRequestHandler: " + type.getName());
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
