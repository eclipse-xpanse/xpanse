/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.register.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        assertNull(SensitiveScope.ALWAYS.getByValue("null"));
    }

    @Test
    void testToValue() {
        assertEquals("none", SensitiveScope.NONE.toValue());
        assertEquals("once", SensitiveScope.ONCE.toValue());
        assertEquals("always", SensitiveScope.ALWAYS.toValue());
    }

}
