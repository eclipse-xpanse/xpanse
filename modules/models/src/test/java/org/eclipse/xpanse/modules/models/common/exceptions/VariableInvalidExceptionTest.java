/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.common.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.VariableInvalidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of VariableInvalidException.
 */
public class VariableInvalidExceptionTest {

    private static List<String> errorReasons;
    private static List<String> errorReasons1;
    private static List<String> errorReasons2;
    private static VariableInvalidException exception;

    @BeforeEach
    public void setUp() {
        errorReasons = new ArrayList<>();
        errorReasons.add("Reason 1");
        errorReasons.add("Reason 2");

        errorReasons1 = new ArrayList<>();

        errorReasons2 = new ArrayList<>();
        errorReasons2.add("Reason 1");
        errorReasons2.add("Reason 3");

        exception = new VariableInvalidException(errorReasons);
    }

    @Test
    public void testConstructorAndGetErrorReasons() {
        assertEquals(errorReasons, exception.getErrorReasons());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(exception, exception);
        assertEquals(exception.hashCode(), exception.hashCode());

        Object obj = new Object();
        assertNotEquals(exception, obj);
        assertNotEquals(exception, null);
        assertNotEquals(exception.hashCode(), obj.hashCode());

        VariableInvalidException exception1 = new VariableInvalidException(errorReasons1);
        assertNotEquals(exception, exception1);
        assertNotEquals(exception.hashCode(), exception1.hashCode());

        VariableInvalidException exception2 = new VariableInvalidException(errorReasons);
        assertNotEquals(exception, exception2);
        assertNotEquals(exception2, exception1);
        assertNotEquals(exception.hashCode(), exception2.hashCode());
        assertNotEquals(exception2.hashCode(), exception1.hashCode());

        VariableInvalidException exception3 = new VariableInvalidException(errorReasons2);
        assertNotEquals(exception, exception3);
        assertNotEquals(exception3, exception2);
        assertNotEquals(exception3, exception1);
        assertNotEquals(exception.hashCode(), exception3.hashCode());
        assertNotEquals(exception3.hashCode(), exception2.hashCode());
        assertNotEquals(exception3.hashCode(), exception1.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString = "VariableInvalidException(errorReasons=" + errorReasons + ")";

        assertEquals(expectedToString, exception.toString());
    }

}
