/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.common.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test of XpanseUnhandledException. */
class XpanseUnhandledExceptionTest {

    private static final String message = "Unhandled exception occurred";
    private static XpanseUnhandledException exception;

    @BeforeEach
    void setUp() {
        exception = new XpanseUnhandledException(message);
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals(message, exception.getMessage());
    }
}
