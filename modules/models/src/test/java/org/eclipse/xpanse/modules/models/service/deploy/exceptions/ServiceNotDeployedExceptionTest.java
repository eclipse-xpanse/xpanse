/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.deploy.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test of ServiceNotDeployedException. */
class ServiceNotDeployedExceptionTest {

    private static final String message = "service not deployed.";
    private static ServiceNotDeployedException exception;

    @BeforeEach
    void setUp() {
        exception = new ServiceNotDeployedException(message);
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals(message, exception.getMessage());
    }
}
