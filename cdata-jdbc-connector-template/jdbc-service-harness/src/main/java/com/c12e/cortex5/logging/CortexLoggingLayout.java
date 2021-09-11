/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
package com.c12e.cortex5.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.contrib.json.JsonLayoutBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates logs based on expected format for Filebeat/ELK
 * {"@message":"loading database services","@timestamp":"2018-11-26T13:57:30.711Z","@fields":{"level":"info","label":"catalog"}}
 */
public class CortexLoggingLayout extends JsonLayoutBase<ILoggingEvent> {
    public static final String MESSAGE_ATTR_NAME = "@message";
    public static final String TIMESTAMP_ATTR_NAME = "@timestamp";
    public static final String FIELDS_ATTR_NAME = "@fields";
    public static final String FIELD_LABEL_VALUE = "jdbc-daemon";


    public CortexLoggingLayout(){
        super();
    }

    @Override
    protected Map toJsonMap(ILoggingEvent event) {

        LoggingFields fields = createFieldsObject(event);

        Map<String, Object> map = new LinkedHashMap<>();
        add(MESSAGE_ATTR_NAME, true, event.getMessage(), map);
        addTimestamp(TIMESTAMP_ATTR_NAME, true, event.getTimeStamp(), map);
        map.put(FIELDS_ATTR_NAME, fields);
        return map;
    }

    protected LoggingFields createFieldsObject(ILoggingEvent event){
        LoggingFields fields = new LoggingFields(StringUtils.lowerCase(event.getLevel().toString()));
        return fields;
    }

    public static class LoggingFields{
        @JsonProperty
        String label = FIELD_LABEL_VALUE;
        @JsonProperty
        String level;

        public LoggingFields(String level){
            this.level = level;
        }
    }
}
