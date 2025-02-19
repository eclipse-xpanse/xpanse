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
class DeployVariableDataTypeTest {

    @Test
    void testGetByValue() {
        assertEquals(
                DeployVariableDataType.STRING, DeployVariableDataType.STRING.getByValue("string"));
        assertEquals(
                DeployVariableDataType.NUMBER, DeployVariableDataType.NUMBER.getByValue("number"));
        assertEquals(
                DeployVariableDataType.BOOLEAN,
                DeployVariableDataType.BOOLEAN.getByValue("boolean"));
        assertThrows(
                UnsupportedEnumValueException.class,
                () -> DeployVariableDataType.BOOLEAN.getByValue("null"));
    }

    @Test
    void testToValue() {
        assertEquals("string", DeployVariableDataType.STRING.toValue());
        assertEquals("number", DeployVariableDataType.NUMBER.toValue());
        assertEquals("boolean", DeployVariableDataType.BOOLEAN.toValue());
    }
}
