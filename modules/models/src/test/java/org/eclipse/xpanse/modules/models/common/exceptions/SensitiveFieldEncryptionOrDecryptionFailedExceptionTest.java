package org.eclipse.xpanse.modules.models.common.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SensitiveFieldEncryptionOrDecryptionFailedExceptionTest {

    private static final String message =
            "sensitive field encryption or decryption failed exception occurred";
    private static SensitiveFieldEncryptionOrDecryptionFailedException exception;

    @BeforeEach
    void setUp() {
        exception = new SensitiveFieldEncryptionOrDecryptionFailedException(message);
    }

    @Test
    void testConstructorAndGetMessage() {
        assertEquals(message, exception.getMessage());
    }
}
