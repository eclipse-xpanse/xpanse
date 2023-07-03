/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.credential.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of CredentialsNotFoundException.
 */
class CredentialsNotFoundExceptionTest {

    private static final String message = "Credentials not found.";
    private static CredentialsNotFoundException exception;

    @BeforeEach
    void setUp() {
        exception = new CredentialsNotFoundException(message);
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals(message, exception.getMessage());
    }

}
