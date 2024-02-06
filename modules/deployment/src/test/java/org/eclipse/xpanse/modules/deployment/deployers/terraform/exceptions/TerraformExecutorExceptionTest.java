/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of TerraformExecutorException.
 */
class TerraformExecutorExceptionTest {

    private static final String message = "terraformExecutor exception.";
    private static final Throwable cause = new RuntimeException("Root cause");
    private static final String output = "Error output";
    private static TerraformExecutorException exception1;
    private static TerraformExecutorException exception2;
    private static TerraformExecutorException exception3;

    @BeforeEach
    void setUp() {
        exception1 = new TerraformExecutorException(message);
        exception2 = new TerraformExecutorException(message, cause);
        exception3 = new TerraformExecutorException(message, output);
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals("TFExecutor Exception: " + message, exception1.getMessage());
        assertEquals(cause, exception2.getCause());
        assertEquals("TFExecutor Exception:terraformExecutor exception." + System.lineSeparator()
                        + output,
                exception3.getMessage());
    }

}
