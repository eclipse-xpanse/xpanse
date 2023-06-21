/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of CidrValidator.
 */
class CidrValidatorTest {

    @Test
    public void testValidCidr() {
        CidrValidator cidrValidator = new CidrValidator();
        String validCidr = "192.168.0.0/24";
        ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);
        boolean isValid = cidrValidator.isValid(validCidr, constraintValidatorContext);
        assertTrue(isValid);
    }

    @Test
    public void testInvalidCidr() {
        CidrValidator cidrValidator = new CidrValidator();
        String invalidCidr = "192.168.0.0/33";
        ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);
        boolean isValid = cidrValidator.isValid(invalidCidr, constraintValidatorContext);
        assertFalse(isValid);
    }
}
