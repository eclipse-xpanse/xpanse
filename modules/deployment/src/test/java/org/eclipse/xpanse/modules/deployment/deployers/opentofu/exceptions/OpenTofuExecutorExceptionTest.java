package org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpenTofuExecutorExceptionTest {

    private static final String message = "OpenTofuExecutor exception.";
    private static final Throwable cause = new RuntimeException("Root cause");
    private static final String output = "Error output";
    private static OpenTofuExecutorException exception1;
    private static OpenTofuExecutorException exception2;
    private static OpenTofuExecutorException exception3;

    @BeforeEach
    void setUp() {
        exception1 = new OpenTofuExecutorException(message);
        exception2 = new OpenTofuExecutorException(message, cause);
        exception3 = new OpenTofuExecutorException(message, output);
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals("OpenTofuExecutor Exception: " + message, exception1.getMessage());
        assertEquals(cause, exception2.getCause());
        assertEquals(
                "OpenTofuExecutor Exception:OpenTofuExecutor exception."
                        + System.lineSeparator()
                        + output,
                exception3.getMessage());
    }
}
