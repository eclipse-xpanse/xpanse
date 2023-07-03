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
 * Test of OpenApiFileGenerationException.
 */
class OpenApiFileGenerationExceptionTest {

    private static final String message = "Failed to generate OpenAPI file";
    private static OpenApiFileGenerationException exception;

    @BeforeEach
    void setUp() {
        exception = new OpenApiFileGenerationException(message);
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals(message, exception.getMessage());
    }

}
