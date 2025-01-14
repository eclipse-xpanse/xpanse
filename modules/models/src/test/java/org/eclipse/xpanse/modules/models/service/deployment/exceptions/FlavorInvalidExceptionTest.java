/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.deployment.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test of FlavorInvalidException. */
class FlavorInvalidExceptionTest {

    private static final String message = "flavor is invalid.";
    private static FlavorInvalidException exception;

    @BeforeEach
    void setUp() {
        exception = new FlavorInvalidException(message);
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals(message, exception.getMessage());
    }
}
