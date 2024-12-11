/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test of ModificationImpact. */
public class ModificationImpactTest {

    private static final Boolean isDataLost = true;

    private static final Boolean isServiceInterrupted = true;

    private static ModificationImpact modificationImpact;

    @BeforeEach
    void setUp() {
        modificationImpact = new ModificationImpact();
        modificationImpact.setIsDataLost(isDataLost);
        modificationImpact.setIsServiceInterrupted(isServiceInterrupted);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(isDataLost, modificationImpact.getIsDataLost());
        assertEquals(isServiceInterrupted, modificationImpact.getIsServiceInterrupted());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(modificationImpact, modificationImpact);
        assertEquals(modificationImpact.hashCode(), modificationImpact.hashCode());

        Object obj = new Object();
        assertNotEquals(modificationImpact, obj);
        assertNotEquals(modificationImpact, null);
        assertNotEquals(modificationImpact.hashCode(), obj.hashCode());

        ModificationImpact modificationImpact1 = new ModificationImpact();
        ModificationImpact modificationImpact2 = new ModificationImpact();
        assertNotEquals(modificationImpact, modificationImpact1);
        assertNotEquals(modificationImpact, modificationImpact2);
        assertEquals(modificationImpact1, modificationImpact2);
        assertNotEquals(modificationImpact.hashCode(), modificationImpact1.hashCode());
        assertNotEquals(modificationImpact.hashCode(), modificationImpact2.hashCode());
        assertEquals(modificationImpact1.hashCode(), modificationImpact2.hashCode());

        modificationImpact1.setIsDataLost(isDataLost);
        assertNotEquals(modificationImpact, modificationImpact1);
        assertNotEquals(modificationImpact1, modificationImpact2);
        assertNotEquals(modificationImpact.hashCode(), modificationImpact1.hashCode());
        assertNotEquals(modificationImpact1.hashCode(), modificationImpact2.hashCode());

        modificationImpact1.setIsServiceInterrupted(isServiceInterrupted);
        assertEquals(modificationImpact, modificationImpact1);
        assertNotEquals(modificationImpact1, modificationImpact2);
        assertEquals(modificationImpact.hashCode(), modificationImpact1.hashCode());
        assertNotEquals(modificationImpact1.hashCode(), modificationImpact2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString =
                "ModificationImpact("
                        + "isDataLost="
                        + isDataLost
                        + ", isServiceInterrupted="
                        + isServiceInterrupted
                        + ")";
        assertEquals(expectedString, modificationImpact.toString());
    }
}
