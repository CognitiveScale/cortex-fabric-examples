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
import com.c12e.cortex5.plugin.SystemEventHandler;

/**
 * User defined {@link SystemEventHandler}.  The {@link PluginHelper} is available for use.
 *
 * In the default implementation the {@link java.sql.Driver} connection properties are unknown at system start so initializing
 * the connection pool is not possible.  The systemInit method is a good place to initialize the connection pool if this information is available.
 *
 * The systemExit method is a good location to close the connection pool
 */
public class CDataSystemEventHandler implements SystemEventHandler {

    @Override
    public void systemInit(PluginHelper helper) {
        helper.getLogger(this.getClass()).info("In plugin start handler");
    }

    @Override
    public void systemExit(PluginHelper helper) {
        helper.getLogger(this.getClass()).info("In plugin exit handler");
    }
}
