/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of UnavailableServiceTemplateException.
 */
class UnavailableServiceTemplateExceptionTest {

    private static final String message = "service template is unavailable.";
    private static UnavailableServiceTemplateException exception;

    @BeforeEach
    void setUp() {
        exception = new UnavailableServiceTemplateException(message);
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(exception, exception);
        assertEquals(exception.hashCode(), exception.hashCode());

        Object obj = new Object();
        assertNotEquals(exception, obj);
        assertNotEquals(exception, null);
        assertNotEquals(exception.hashCode(), obj.hashCode());

        UnavailableServiceTemplateException exception1 =
                new UnavailableServiceTemplateException(message);
        UnavailableServiceTemplateException exception2 =
                new UnavailableServiceTemplateException("different message");

        assertNotEquals(exception, exception1);
        assertNotEquals(exception, exception2);
        assertNotEquals(exception1, exception2);
        assertNotEquals(exception.hashCode(), exception1.hashCode());
        assertNotEquals(exception.hashCode(), exception2.hashCode());
        assertNotEquals(exception1.hashCode(), exception2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString = exception.getClass().getCanonicalName() + ": " + message;

        assertEquals(expectedToString, exception.toString());
    }

    @Test
    void testSuperClassMethods() {
        String expectedSuperToString = exception.getClass().getCanonicalName() + ": " + message;
        assertEquals(expectedSuperToString, exception.toString());

        assertEquals(message, exception.getMessage());
    }

}
