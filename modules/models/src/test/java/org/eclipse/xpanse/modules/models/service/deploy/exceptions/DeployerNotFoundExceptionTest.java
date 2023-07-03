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
 * Test of DeployerNotFoundException.
 */
class DeployerNotFoundExceptionTest {

    private static final String message = "deployer not found.";
    private static DeployerNotFoundException exception;

    @BeforeEach
    void setUp() {
        exception = new DeployerNotFoundException(message);
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals(message, exception.getMessage());
    }

}
