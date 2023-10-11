package org.eclipse.xpanse.api.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResponseValidatorTest {

    @Mock
    private Validator mockValidator;

    private ResponseValidator responseValidatorUnderTest;

    @BeforeEach
    void setUp() {
        responseValidatorUnderTest = new ResponseValidator(mockValidator);
    }

    @Test
    void testValidateResponseData() {

        // Setup
        when(mockValidator.validate(any(Object.class))).thenReturn(new HashSet<>());

        List<String> validators = new ArrayList<>();
        validators.add("MinLength=8");
        validators.add("MaxLength=16");
        // Run the test
        responseValidatorUnderTest.validateResponseData(validators);

    }

    @Test
    void testValidateResponseDataWithValidator() {
        // Setup
        when(mockValidator.validate(any(Object.class))).thenReturn(Collections.emptySet());

        // Run the test
        responseValidatorUnderTest.validateResponseData("MinLength=8");
    }
}
