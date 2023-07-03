/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.deploy.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of InvalidServiceStateException.
 */
class InvalidServiceStateExceptionTest {

    private static final String message = "serviceState is invalid.";
    private static InvalidServiceStateException exception;

    @BeforeEach
    void setUp() {
        exception = new InvalidServiceStateException(message);
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals(message, exception.getMessage());
    }

}
