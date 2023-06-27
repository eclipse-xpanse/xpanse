/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.monitor.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test of ClientApiCallFailedException.
 */
class ClientApiCallFailedExceptionTest {

    @Test
    public void testConstructorAndGetMessage() {
        String message = "Client API call failed.";
        ClientApiCallFailedException exception = new ClientApiCallFailedException(message);

        Assertions.assertEquals(message, exception.getMessage(),
                "Exception message does not match the expected value.");
    }

    @Test
    public void testToString() {
        String message = "Client API call failed.";
        ClientApiCallFailedException exception = new ClientApiCallFailedException(message);

        String expectedToString = exception.getClass().getCanonicalName() + ": " + message;
        String actualToString = exception.toString();

        Assertions.assertEquals(expectedToString, actualToString,
                "Exception toString() does not match the expected string.");
    }

}
