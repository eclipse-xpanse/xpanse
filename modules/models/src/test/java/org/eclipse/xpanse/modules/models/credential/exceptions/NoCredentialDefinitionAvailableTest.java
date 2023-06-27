/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.credential.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Test of NoCredentialDefinitionAvailable.
 */
class NoCredentialDefinitionAvailableTest {

    @Test
    public void testConstructorAndGetMessage() {
        String message = "No credential definition available.";
        NoCredentialDefinitionAvailable exception = new NoCredentialDefinitionAvailable(message);

        assertEquals(message, exception.getMessage(),
                "Exception message does not match the expected value.");
    }

    @Test
    public void testToString() {
        String message = "No credential definition available.";
        NoCredentialDefinitionAvailable exception = new NoCredentialDefinitionAvailable(message);

        String expectedToString = exception.getClass().getCanonicalName() + ": " + message;
        String actualToString = exception.toString();

        assertEquals(expectedToString, actualToString,
                "Exception toString() does not match the expected string.");
    }

    @Test
    public void testInheritedMethods() {
        String message = "No credential definition available.";
        NoCredentialDefinitionAvailable exception = new NoCredentialDefinitionAvailable(message);

        assertEquals(message, exception.getMessage(),
                "Exception message does not match the expected value.");
        assertEquals(message, exception.getLocalizedMessage(),
                "Localized exception message does not match the expected value.");
        assertNull(exception.getCause(), "Exception cause should be null.");
    }

}
