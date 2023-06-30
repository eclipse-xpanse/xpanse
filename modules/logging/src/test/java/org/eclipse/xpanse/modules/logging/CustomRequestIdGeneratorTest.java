package org.eclipse.xpanse.modules.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

class CustomRequestIdGeneratorTest {

    private CustomRequestIdGenerator customRequestIdGeneratorUnderTest;

    @BeforeEach
    void setUp() {
        customRequestIdGeneratorUnderTest = new CustomRequestIdGenerator();
    }

    @Test
    void testGenerate() {
        assertNotNull(customRequestIdGeneratorUnderTest.generate(any()));
    }
}
