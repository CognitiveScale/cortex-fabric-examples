/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
package com.c12e.cortex5.plugin.cdata;

import com.c12e.cortex5.RequestMapping;
import com.c12e.cortex5.loader.RequestHandlerLoader;
import com.c12e.cortex5.plugin.JdbcRequestHandler;

import java.util.Map;

public class CDataTestSystemEventHandler extends CDataSystemEventHandler {
    CDataTestHandlerHelper testHandlerHelper;
    Map<RequestMapping, JdbcRequestHandler> requestMap;

    public void onStartup() {
        testHandlerHelper = new CDataTestHandlerHelper();
        requestMap = new RequestHandlerLoader(testHandlerHelper).getValidRequestHandlers(this.getClass().getClassLoader());

        super.systemInit(testHandlerHelper);
    }

    public void onShutdown() {
        super.systemExit(testHandlerHelper);
    }

    public Map<RequestMapping, JdbcRequestHandler> getHandlerMap(){
        return requestMap;
    }
}
