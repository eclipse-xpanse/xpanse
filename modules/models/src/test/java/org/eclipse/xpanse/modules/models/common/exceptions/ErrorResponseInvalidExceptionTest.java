/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.common.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of ResponseInvalidException.
 */
class ResponseInvalidExceptionTest {

    private static List<String> errorReasons;
    private static List<String> errorReasons1;
    private static List<String> errorReasons2;
    private static ResponseInvalidException exception;

    @BeforeEach
    public void setUp() {
        errorReasons = new ArrayList<>();
        errorReasons.add("Reason 1");
        errorReasons.add("Reason 2");

        errorReasons1 = new ArrayList<>();

        errorReasons2 = new ArrayList<>();
        errorReasons2.add("Reason 1");
        errorReasons2.add("Reason 3");

        exception = new ResponseInvalidException(errorReasons);
    }

    @Test
    public void testConstructorAndGetErrorReasons() {
        assertEquals(errorReasons, exception.getErrorReasons());
    }

    @Test
    public void testConstructorAndGetMessage() {
        assertNull(exception.getMessage());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(exception, exception);
        assertEquals(exception.hashCode(), exception.hashCode());

        Object obj = new Object();
        assertNotEquals(exception, obj);
        assertNotEquals(exception, null);
        assertNotEquals(exception.hashCode(), obj.hashCode());

        ResponseInvalidException exception1 = new ResponseInvalidException(errorReasons1);
        assertNotEquals(exception, exception1);
        assertNotEquals(exception.hashCode(), exception1.hashCode());

        ResponseInvalidException exception2 = new ResponseInvalidException(errorReasons);
        assertNotEquals(exception, exception2);
        assertNotEquals(exception2, exception1);
        assertNotEquals(exception.hashCode(), exception2.hashCode());
        assertNotEquals(exception2.hashCode(), exception1.hashCode());

        ResponseInvalidException exception3 = new ResponseInvalidException(errorReasons2);
        assertNotEquals(exception, exception3);
        assertNotEquals(exception3, exception2);
        assertNotEquals(exception3, exception1);
        assertNotEquals(exception.hashCode(), exception3.hashCode());
        assertNotEquals(exception3.hashCode(), exception2.hashCode());
        assertNotEquals(exception3.hashCode(), exception1.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString = "ResponseInvalidException(errorReasons=" + errorReasons + ")";

        assertEquals(expectedToString, exception.toString());
    }

}
