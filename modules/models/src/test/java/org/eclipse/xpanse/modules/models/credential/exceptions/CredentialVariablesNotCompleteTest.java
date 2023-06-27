/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.credential.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of CredentialVariablesNotComplete.
 */
class CredentialVariablesNotCompleteTest {

    private static final Set<String> errorReasons1 = new HashSet<>();
    private static final Set<String> errorReasons2 = new HashSet<>();
    private static final Set<String> errorReasons3 = new HashSet<>();

    @BeforeEach
    public void setUp() {
        errorReasons1.add("Reason 1");
        errorReasons1.add("Reason 2");

        errorReasons2.add("Reason 1");
        errorReasons2.add("Reason 2");

        errorReasons3.add("Reason 1");
        errorReasons3.add("Reason 3");
    }

    @Test
    public void testConstructorAndGetErrorReasons() {
        CredentialVariablesNotComplete exception =
                new CredentialVariablesNotComplete(errorReasons1);
        assertEquals(errorReasons2, exception.getErrorReasons());
    }

    @Test
    public void testConstructorAndGetMessage() {
        CredentialVariablesNotComplete exception =
                new CredentialVariablesNotComplete(errorReasons1);

        String expectedMessage =
                String.format("Credential Variables Not Complete. Error reasons: %s",
                        errorReasons1);
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
        CredentialVariablesNotComplete exception1 =
                new CredentialVariablesNotComplete(errorReasons1);
        CredentialVariablesNotComplete exception2 =
                new CredentialVariablesNotComplete(errorReasons2);
        CredentialVariablesNotComplete exception3 =
                new CredentialVariablesNotComplete(errorReasons3);
        CredentialVariablesNotComplete exception4 =
                new CredentialVariablesNotComplete(new HashSet<>());

        assertEquals(exception1, exception1);
        assertNotEquals(exception1, exception2);
        assertNotEquals(exception1, exception3);
        assertNotEquals(exception1, exception4);
        assertNotEquals(exception1, null);
        assertNotEquals(exception1, "Not a CredentialVariablesNotComplete instance");

        assertEquals(exception1.hashCode(), exception1.hashCode());
        assertNotEquals(exception1.hashCode(), exception2.hashCode());
        assertNotEquals(exception1.hashCode(), exception3.hashCode());
        assertNotEquals(exception1.hashCode(), exception4.hashCode());
    }

    @Test
    public void testToString() {
        CredentialVariablesNotComplete exception =
                new CredentialVariablesNotComplete(errorReasons1);

        String expectedToString =
                "CredentialVariablesNotComplete(errorReasons=" + errorReasons1 + ")";
        assertEquals(expectedToString, exception.toString());
    }

    @Test
    public void testCanEqual() {
        CredentialVariablesNotComplete exception =
                new CredentialVariablesNotComplete(errorReasons1);

        assertTrue(exception.canEqual(new CredentialVariablesNotComplete(errorReasons1)));
        assertFalse(exception.canEqual(new RuntimeException()));
    }

    @Test
    public void testInheritedMethods() {
        CredentialVariablesNotComplete exception =
                new CredentialVariablesNotComplete(errorReasons1);

        assertEquals("Credential Variables Not Complete. Error reasons: [Reason 1, Reason 2]",
                exception.getMessage(),
                "Exception message does not match the expected value.");
        assertEquals("Credential Variables Not Complete. Error reasons: [Reason 1, Reason 2]",
                exception.getLocalizedMessage(),
                "Localized exception message does not match the expected value.");
        assertNull(exception.getCause(), "Exception cause should be null.");
    }

}
