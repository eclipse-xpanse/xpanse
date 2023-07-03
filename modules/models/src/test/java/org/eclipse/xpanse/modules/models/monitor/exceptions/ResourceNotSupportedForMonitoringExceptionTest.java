/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.monitor.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of ResourceNotSupportedForMonitoringException.
 */
class ResourceNotSupportedForMonitoringExceptionTest {

    private static final String message = "Resource not support";
    private static ResourceNotSupportedForMonitoringException exception;

    @BeforeEach
    void setUp() {
        exception = new ResourceNotSupportedForMonitoringException(message);
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals(message, exception.getMessage());
    }

}
