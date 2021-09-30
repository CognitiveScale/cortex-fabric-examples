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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * HTTP request mapping for Cortex REST service creation.
 * There must be one {@link JdbcRequestHandler} declared with the default path and requestType.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    /**
     * Relative path to access the {@link JdbcRequestHandler}
     * @return request path
     */
    String path() default "/invoke";

    /**
     * HTTP request method type @{@link HttpRequestType}
     * @return request HTTP type
     */
    HttpRequestType requestType() default HttpRequestType.POST;

}