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
 * Test of MetricsDataNotYetAvailableException.
 */
public class MetricsDataNotYetAvailableExceptionTest {

    private static final String message = "Metrics data not ready.";
    private static MetricsDataNotYetAvailableException exception;

    @BeforeEach
    void setUp() {
        exception = new MetricsDataNotYetAvailableException(message);
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals(message, exception.getMessage());
    }
}
