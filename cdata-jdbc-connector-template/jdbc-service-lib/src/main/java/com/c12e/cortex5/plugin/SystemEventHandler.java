/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
package com.c12e.cortex5.plugin;

import com.c12e.cortex5.PluginHelper;

/**
 * Hook for injected logic at specific system events.  Must be extended in the plugin.
 */
public interface SystemEventHandler {

    /**
     * Called on system startup.  Plugin JAR will be loaded by the time this is called.  Set system variables, connection manager, ...
     *
     * @param helper {@link PluginHelper}
     */
    void systemInit(PluginHelper helper);

    /**
     * Called on system shutdown.  Close connection(s) during this event.
     *
     * @param helper {@link PluginHelper}
     */
    void systemExit(PluginHelper helper);

}
