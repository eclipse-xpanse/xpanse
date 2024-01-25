package org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TerraformProviderNotFoundExceptionTest {

    private TerraformProviderNotFoundException exception;

    @BeforeEach
    void setUp() {
        exception = new TerraformProviderNotFoundException("message");
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals("message", exception.getMessage());
    }
}
