/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.register.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

/**
 * Test of DeployerKind.
 */
class DeployerKindTest {

    @Test
    void testGetByValue() {
        assertEquals(DeployerKind.TERRAFORM, DeployerKind.TERRAFORM.getByValue("terraform"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> DeployerKind.TERRAFORM.getByValue("null"));
    }

    @Test
    void testToValue() {
        assertEquals("terraform", DeployerKind.TERRAFORM.toValue());
    }

}
