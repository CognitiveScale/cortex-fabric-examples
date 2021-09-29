/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
package com.c12e.cortex5.provider;

import com.c12e.cortex5.RequestMapping;
import com.c12e.cortex5.handler.RequestWrapper;
import com.c12e.cortex5.loader.RequestHandlerLoader;
import com.c12e.cortex5.plugin.JdbcRequestHandler;
import com.c12e.cortex5.plugin.SystemEventHandler;
import com.c12e.cortex5.utility.CortexPluginHelper;
import com.c12e.cortex5.utility.HarnessPluginHelper;
import com.networknt.health.HealthGetHandler;
import com.networknt.handler.HandlerProvider;
import com.networknt.server.ShutdownHookProvider;
import com.networknt.server.StartupHookProvider;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.util.Methods;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CortexSystemEventHandler implements StartupHookProvider, ShutdownHookProvider, HandlerProvider {
    static final String SHUTDOWN_PROVIDER_CLASSNAME = "com.networknt.server.ShutdownHookProvider";
    static final String HANDLER_PROVIDER_CLASSNAME = "com.networknt.handler.HandlerProvider";


    SystemEventHandler pluginEventHandler;
    HarnessPluginHelper pluginHelper;
    RoutingHandler routingHandler;

    @Override
    public void onStartup() {
        try {
            SingletonServiceFactory.setBean(SHUTDOWN_PROVIDER_CLASSNAME, this);
            SingletonServiceFactory.setBean(HANDLER_PROVIDER_CLASSNAME, this);

            HarnessPluginHelper pluginHelper = getPluginHelper();
            loadPlugin(pluginHelper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void loadPlugin(HarnessPluginHelper pluginHelper) throws Exception {
        String jarLocation = System.getenv("plugin.jar");
        String startupToken = System.getenv("startup.token");
        pluginHelper.setJwt(startupToken);
        File pluginJar = null;
        if (StringUtils.isNotBlank(jarLocation)) {
            pluginJar = pluginHelper.getManagedContent(jarLocation);
        } else {
            pluginJar = new File("plugin.jar");
        }

        System.out.println("Plugin location : " + pluginJar.toURI().toURL().toString());
        pluginHelper.addToClasspath(pluginJar.toURI().toURL());
        Reflections reflections = new Reflections(this.getClass().getClassLoader());
        Set<Class<? extends SystemEventHandler>> eventHandlers = reflections.getSubTypesOf(SystemEventHandler.class);

        if (!(eventHandlers.size() == 1)) {
            throw new RuntimeException();
        }
        // java 11
        // pluginEventHandler = eventHandlers.stream().findFirst().get().getDeclaredConstructor().newInstance();
        pluginEventHandler = eventHandlers.stream().findFirst().get().newInstance();
        System.out.println("Found event handler: " + pluginEventHandler.getClass().getName());
        pluginEventHandler.systemInit(pluginHelper);
    }

    @Override
    public void onShutdown() {
        if (Objects.nonNull(pluginEventHandler)) {
            pluginEventHandler.systemExit(getPluginHelper());
        }
    }

    @Override
    public HttpHandler getHandler() {
        RoutingHandler routingHandler = getRoutingHandler();

        loadPluginRoutes(routingHandler);

        return routingHandler;
    }

    protected void loadPluginRoutes(RoutingHandler routingHandler) {
        RequestHandlerLoader requestHandlerLoader = new RequestHandlerLoader(getPluginHelper());
        Map<RequestMapping, JdbcRequestHandler> requestHandlerMap = requestHandlerLoader.getValidRequestHandlers(this.getClass().getClassLoader());

        for (RequestMapping requestMapping : requestHandlerMap.keySet()) {
            System.out.println("Adding request mapping for path : " + requestMapping.path() + " method: " + requestMapping.requestType());
            RequestWrapper requestWrapper = new RequestWrapper(requestHandlerMap.get(requestMapping), getPluginHelper());
            routingHandler.add(requestMapping.requestType().name(), requestMapping.path(), requestWrapper);
        }
    }

    protected HarnessPluginHelper getPluginHelper() {
        if (Objects.isNull(pluginHelper)) {
            pluginHelper = new CortexPluginHelper();
        }
        return pluginHelper;
    }

    protected RoutingHandler getRoutingHandler() {
        if (Objects.isNull(routingHandler)) {
            routingHandler = Handlers.routing()
                    .add(Methods.GET, "/health", new HealthGetHandler());
        }
        return routingHandler;
    }
}
