/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

/** Test of DeployVariableDataType. */
class VariableDataTypeTest {

    @Test
    void testGetByValue() {
        assertEquals(VariableDataType.STRING, VariableDataType.STRING.getByValue("string"));
        assertEquals(VariableDataType.NUMBER, VariableDataType.NUMBER.getByValue("number"));
        assertEquals(VariableDataType.BOOLEAN, VariableDataType.BOOLEAN.getByValue("boolean"));
        assertThrows(
                UnsupportedEnumValueException.class,
                () -> VariableDataType.BOOLEAN.getByValue("null"));
    }

    @Test
    void testToValue() {
        assertEquals("string", VariableDataType.STRING.toValue());
        assertEquals("number", VariableDataType.NUMBER.toValue());
        assertEquals("boolean", VariableDataType.BOOLEAN.toValue());
    }
}
