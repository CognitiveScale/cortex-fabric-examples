/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
package com.c12e.cortex5.plugin.cdata.test;

import com.c12e.cortex5.RequestMapping;
import com.c12e.cortex5.plugin.cdata.CDataTestSystemEventHandler;
import com.c12e.cortex5.plugin.JdbcRequestHandler;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class CDataSystemProviderTest {

    @Test
    public void testSystemProvider() throws Exception {
        CDataTestSystemEventHandler systemProvider = new CDataTestSystemEventHandler();
        systemProvider.onStartup();
        Map<RequestMapping, JdbcRequestHandler> requestMap = systemProvider.getHandlerMap();
        Assert.assertEquals(1, requestMap.size());
        systemProvider.onShutdown();
    }
}
