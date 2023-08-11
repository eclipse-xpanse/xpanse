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

/**
 * Test of SensitiveScope.
 */
class SensitiveScopeTest {

    @Test
    void testGetByValue() {
        assertEquals(SensitiveScope.NONE, SensitiveScope.NONE.getByValue("none"));
        assertEquals(SensitiveScope.ONCE, SensitiveScope.ONCE.getByValue("once"));
        assertEquals(SensitiveScope.ALWAYS, SensitiveScope.ALWAYS.getByValue("always"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> SensitiveScope.ALWAYS.getByValue("null"));
    }

    @Test
    void testToValue() {
        assertEquals("none", SensitiveScope.NONE.toValue());
        assertEquals("once", SensitiveScope.ONCE.toValue());
        assertEquals("always", SensitiveScope.ALWAYS.toValue());
    }

}
