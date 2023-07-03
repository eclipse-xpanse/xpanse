/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.credential.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of CredentialVariablesNotComplete.
 */
class CredentialVariablesNotCompleteTest {

    private static Set<String> errorReasons;
    private static Set<String> errorReasons2;
    private static CredentialVariablesNotComplete exception;

    @BeforeEach
    public void setUp() {
        errorReasons = new HashSet<>();
        errorReasons.add("Reason 1");
        errorReasons.add("Reason 2");

        errorReasons2 = new HashSet<>();
        errorReasons2.add("Reason 1");
        errorReasons2.add("Reason 3");

        exception = new CredentialVariablesNotComplete(errorReasons);
    }

    @Test
    public void testConstructorAndGetErrorReasons() {
        assertEquals(errorReasons, exception.getErrorReasons());
    }

    @Test
    public void testConstructorAndGetMessage() {
        String expectedMessage =
                String.format("Credential Variables Not Complete. Error reasons: %s",
                        errorReasons);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testConstructorWithEmptyErrorReasons() {
        Set<String> errorReasons = new HashSet<>();
        CredentialVariablesNotComplete exception = new CredentialVariablesNotComplete(errorReasons);

        assertEquals(errorReasons, exception.getErrorReasons());
    }

    @Test
    public void testEqualsAndHashCode() {
        assertEquals(exception, exception);
        assertEquals(exception.hashCode(), exception.hashCode());

        Object obj = new Object();
        assertNotEquals(exception, obj);
        assertNotEquals(exception, null);
        assertNotEquals(exception.hashCode(), obj.hashCode());

        CredentialVariablesNotComplete exception1 =
                new CredentialVariablesNotComplete(errorReasons);
        CredentialVariablesNotComplete exception2 =
                new CredentialVariablesNotComplete(errorReasons2);
        assertNotEquals(exception, exception1);
        assertNotEquals(exception, exception2);
        assertNotEquals(exception1, exception2);
        assertNotEquals(exception.hashCode(), exception1.hashCode());
        assertNotEquals(exception.hashCode(), exception2.hashCode());
        assertNotEquals(exception1.hashCode(), exception2.hashCode());
    }

    @Test
    public void testToString() {
        String expectedToString =
                "CredentialVariablesNotComplete(errorReasons=" + errorReasons + ")";
        assertEquals(expectedToString, exception.toString());
    }

    @Test
    public void testInheritedMethods() {
        assertEquals("Credential Variables Not Complete. Error reasons: [Reason 1, Reason 2]",
                exception.getMessage(),
                "Exception message does not match the expected value.");
        assertEquals("Credential Variables Not Complete. Error reasons: [Reason 1, Reason 2]",
                exception.getLocalizedMessage(),
                "Localized exception message does not match the expected value.");
        assertNull(exception.getCause(), "Exception cause should be null.");
    }

}
