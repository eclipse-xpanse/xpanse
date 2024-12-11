/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.credential.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test of CredentialCapabilityNotFound. */
class CredentialCapabilityNotFoundTest {

    private static final String message = "Credential capability not found.";
    private static CredentialCapabilityNotFound exception;

    @BeforeEach
    void setUp() {
        exception = new CredentialCapabilityNotFound(message);
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals(message, exception.getMessage());
    }
}
