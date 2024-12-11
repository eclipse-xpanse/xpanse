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

/** Test of DeployVariableKind. */
class DeployVariableKindTest {

    @Test
    void testGetByValue() {
        assertEquals(DeployVariableKind.FIX_ENV, DeployVariableKind.FIX_ENV.getByValue("fix_env"));
        assertEquals(
                DeployVariableKind.FIX_VARIABLE,
                DeployVariableKind.FIX_VARIABLE.getByValue("fix_variable"));
        assertEquals(DeployVariableKind.ENV, DeployVariableKind.ENV.getByValue("env"));
        assertEquals(
                DeployVariableKind.VARIABLE, DeployVariableKind.VARIABLE.getByValue("variable"));
        assertEquals(DeployVariableKind.ENV_ENV, DeployVariableKind.ENV_ENV.getByValue("env_env"));
        assertEquals(
                DeployVariableKind.ENV_VARIABLE,
                DeployVariableKind.ENV_VARIABLE.getByValue("env_variable"));
        assertThrows(
                UnsupportedEnumValueException.class,
                () -> DeployVariableKind.ENV_VARIABLE.getByValue("null"));
    }

    @Test
    void testToValue() {
        assertEquals("fix_env", DeployVariableKind.FIX_ENV.toValue());
        assertEquals("fix_variable", DeployVariableKind.FIX_VARIABLE.toValue());
        assertEquals("env", DeployVariableKind.ENV.toValue());
        assertEquals("variable", DeployVariableKind.VARIABLE.toValue());
        assertEquals("env_env", DeployVariableKind.ENV_ENV.toValue());
        assertEquals("env_variable", DeployVariableKind.ENV_VARIABLE.toValue());
    }
}
