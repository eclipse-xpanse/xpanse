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

/**
 * Test of FlavorBasic.
 */
class FlavorBasicTest {

    private static final String name = "flavor";
    private static final Integer fixedPrice = 1;
    private static FlavorBasic flavorBasic;

    @BeforeEach
    void setUp() {
        flavorBasic = new FlavorBasic();
        flavorBasic.setName(name);
        flavorBasic.setFixedPrice(fixedPrice);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(name, flavorBasic.getName());
        assertEquals(fixedPrice, flavorBasic.getFixedPrice());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(flavorBasic, flavorBasic);
        assertEquals(flavorBasic.hashCode(), flavorBasic.hashCode());

        Object obj = new Object();
        assertNotEquals(flavorBasic, obj);
        assertNotEquals(flavorBasic, null);
        assertNotEquals(flavorBasic.hashCode(), obj.hashCode());

        FlavorBasic flavorBasic1 = new FlavorBasic();
        FlavorBasic flavorBasic2 = new FlavorBasic();
        assertNotEquals(flavorBasic, flavorBasic1);
        assertNotEquals(flavorBasic, flavorBasic2);
        assertEquals(flavorBasic1, flavorBasic2);
        assertNotEquals(flavorBasic.hashCode(), flavorBasic1.hashCode());
        assertNotEquals(flavorBasic.hashCode(), flavorBasic2.hashCode());
        assertEquals(flavorBasic1.hashCode(), flavorBasic2.hashCode());

        flavorBasic1.setName(name);
        assertNotEquals(flavorBasic, flavorBasic1);
        assertNotEquals(flavorBasic1, flavorBasic2);
        assertNotEquals(flavorBasic.hashCode(), flavorBasic1.hashCode());
        assertNotEquals(flavorBasic1.hashCode(), flavorBasic2.hashCode());

        flavorBasic1.setFixedPrice(fixedPrice);
        assertEquals(flavorBasic, flavorBasic1);
        assertNotEquals(flavorBasic1, flavorBasic2);
        assertEquals(flavorBasic.hashCode(), flavorBasic1.hashCode());
        assertNotEquals(flavorBasic1.hashCode(), flavorBasic2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "FlavorBasic(" +
                "name=" + name +
                ", fixedPrice=" + fixedPrice + ")";
        assertEquals(expectedString, flavorBasic.toString());
    }

}
