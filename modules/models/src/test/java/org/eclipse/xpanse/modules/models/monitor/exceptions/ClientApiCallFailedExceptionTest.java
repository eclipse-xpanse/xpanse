/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.monitor.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.xpanse.modules.models.common.exceptions.ClientApiCallFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of ClientApiCallFailedException.
 */
class ClientApiCallFailedExceptionTest {

    private static final String message = "clientApi call failed.";
    private static ClientApiCallFailedException exception;

    @BeforeEach
    void setUp() {
        exception = new ClientApiCallFailedException(message);
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals(message, exception.getMessage());
    }

}
