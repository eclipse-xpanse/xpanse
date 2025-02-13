package org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TerraBootRequestFailedExceptionTest {

    private TerraBootRequestFailedException exception;

    @BeforeEach
    void setUp() {
        exception = new TerraBootRequestFailedException("message");
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals("TFExecutor Exception: message", exception.getMessage());
    }
}
