/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
package com.c12e.cortex5.utility;

import com.c12e.cortex5.PluginHelper;

import java.net.URL;

public interface HarnessPluginHelper extends PluginHelper {

    void setJwt(String jwt);

    void setApiEndpoint(String apiEndpoint);

    void addToClasspath(URL path);

    String getImplementationVersion();
}
