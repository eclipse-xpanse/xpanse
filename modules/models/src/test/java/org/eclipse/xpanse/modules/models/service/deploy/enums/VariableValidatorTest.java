/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.deploy.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Test of VariableValidator.
 */
class VariableValidatorTest {

    @Test
    void testGetByValue() {
        assertEquals(VariableValidator.MINLENGTH,
                VariableValidator.MINLENGTH.getByValue("minLength"));
        assertEquals(VariableValidator.MAXLENGTH,
                VariableValidator.MAXLENGTH.getByValue("maxLength"));
        assertEquals(VariableValidator.MINIMUM, VariableValidator.MINIMUM.getByValue("minimum"));
        assertEquals(VariableValidator.MAXIMUM, VariableValidator.MAXIMUM.getByValue("maximum"));
        assertEquals(VariableValidator.PATTERN, VariableValidator.PATTERN.getByValue("pattern"));
        assertEquals(VariableValidator.ENUM, VariableValidator.ENUM.getByValue("enum"));
        assertNull(VariableValidator.ENUM.getByValue("unavailable"));
    }

    @Test
    void testToValue() {
        assertEquals("minLength", VariableValidator.MINLENGTH.toValue());
        assertEquals("maxLength", VariableValidator.MAXLENGTH.toValue());
        assertEquals("minimum", VariableValidator.MINIMUM.toValue());
        assertEquals("maximum", VariableValidator.MAXIMUM.toValue());
        assertEquals("pattern", VariableValidator.PATTERN.toValue());
        assertEquals("enum", VariableValidator.ENUM.toValue());
    }

}
