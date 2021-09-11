/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
package com.c12e.cortex5.logging;

import ch.qos.logback.contrib.jackson.JacksonJsonFormatter;

import java.io.IOException;
import java.util.Map;

public class CortexLoggingFormatter extends JacksonJsonFormatter {

    public CortexLoggingFormatter() {
        super();
    }

    @Override
    public String toJsonString(Map m) throws IOException {
        String jsonString = super.toJsonString(m);
        return jsonString + "\n";
    }
}
