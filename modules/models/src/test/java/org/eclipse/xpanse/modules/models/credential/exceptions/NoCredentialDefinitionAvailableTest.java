/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.credential.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test of NoCredentialDefinitionAvailable. */
class NoCredentialDefinitionAvailableTest {

    private static final String message = "No credential definition available.";
    private static NoCredentialDefinitionAvailable exception;

    @BeforeEach
    void setUp() {
        exception = new NoCredentialDefinitionAvailable(message);
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals(message, exception.getMessage());
    }
}
