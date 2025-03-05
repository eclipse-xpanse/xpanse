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

/** Test of VariableKind. */
class VariableKindTest {

    @Test
    void testGetByValue() {
        assertEquals(VariableKind.FIX_ENV, VariableKind.FIX_ENV.getByValue("fix_env"));
        assertEquals(
                VariableKind.FIX_VARIABLE, VariableKind.FIX_VARIABLE.getByValue("fix_variable"));
        assertEquals(VariableKind.ENV, VariableKind.ENV.getByValue("env"));
        assertEquals(VariableKind.VARIABLE, VariableKind.VARIABLE.getByValue("variable"));
        assertEquals(VariableKind.ENV_ENV, VariableKind.ENV_ENV.getByValue("env_env"));
        assertEquals(
                VariableKind.ENV_VARIABLE, VariableKind.ENV_VARIABLE.getByValue("env_variable"));
        assertThrows(
                UnsupportedEnumValueException.class,
                () -> VariableKind.ENV_VARIABLE.getByValue("null"));
    }

    @Test
    void testToValue() {
        assertEquals("fix_env", VariableKind.FIX_ENV.toValue());
        assertEquals("fix_variable", VariableKind.FIX_VARIABLE.toValue());
        assertEquals("env", VariableKind.ENV.toValue());
        assertEquals("variable", VariableKind.VARIABLE.toValue());
        assertEquals("env_env", VariableKind.ENV_ENV.toValue());
        assertEquals("env_variable", VariableKind.ENV_VARIABLE.toValue());
    }
}
