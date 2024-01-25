package org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpenTofuProviderNotFoundExceptionTest {

    private OpenTofuProviderNotFoundException exception;

    @BeforeEach
    void setUp() {
        exception = new OpenTofuProviderNotFoundException("message");
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals("message", exception.getMessage());
    }
}
