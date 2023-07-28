/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.common.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of UnsupportedEnumValueException.
 */
public class UnsupportedEnumValueExceptionTest {

    private static final String message = "Unsupported Enum Value";
    private static UnsupportedEnumValueException exception;

    @BeforeEach
    void setUp() {
        exception = new UnsupportedEnumValueException(message);
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals(message, exception.getMessage());
    }
}
