/**
 * CognitiveScale Cortex
 *
 * Copyright (c) Cognitive Scale Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */
package com.c12e.cortex5.validate;

/**
 * Validator for plugged logic
 *
 * @param <T> the object type to be validated
 */
public interface PluginValidator<T> {

    /**
     * @param value the value to be validated
     * @return if the value is valid
     */
    Boolean isValid(T value);
}
