/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.register;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of Flavor.
 */
class FlavorTest {

    private static final String name = "flavor";
    private static final Integer fixedPrice = 1;
    private static final Map<String, String> properties = Map.of("key", "value");
    private static Flavor flavor;

    @BeforeEach
    void setUp() {
        flavor = new Flavor();
        flavor.setName(name);
        flavor.setFixedPrice(fixedPrice);
        flavor.setProperties(properties);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(name, flavor.getName());
        assertEquals(fixedPrice, flavor.getFixedPrice());
        assertEquals(properties, flavor.getProperties());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(flavor, flavor);
        assertEquals(flavor.hashCode(), flavor.hashCode());

        Object obj = new Object();
        assertNotEquals(flavor, obj);
        assertNotEquals(flavor, null);
        assertNotEquals(flavor.hashCode(), obj.hashCode());

        Flavor flavor1 = new Flavor();
        Flavor flavor2 = new Flavor();
        assertNotEquals(flavor, flavor1);
        assertNotEquals(flavor, flavor2);
        assertEquals(flavor1, flavor2);
        assertNotEquals(flavor.hashCode(), flavor1.hashCode());
        assertNotEquals(flavor.hashCode(), flavor2.hashCode());
        assertEquals(flavor1.hashCode(), flavor2.hashCode());

        flavor1.setName(name);
        assertNotEquals(flavor, flavor1);
        assertNotEquals(flavor1, flavor2);
        assertNotEquals(flavor.hashCode(), flavor1.hashCode());
        assertNotEquals(flavor1.hashCode(), flavor2.hashCode());

        flavor1.setFixedPrice(fixedPrice);
        assertNotEquals(flavor, flavor1);
        assertNotEquals(flavor1, flavor2);
        assertNotEquals(flavor.hashCode(), flavor1.hashCode());
        assertNotEquals(flavor1.hashCode(), flavor2.hashCode());

        flavor1.setProperties(properties);
        assertEquals(flavor, flavor1);
        assertNotEquals(flavor1, flavor2);
        assertEquals(flavor.hashCode(), flavor1.hashCode());
        assertNotEquals(flavor1.hashCode(), flavor2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "Flavor(" +
                "name=" + name +
                ", fixedPrice=" + fixedPrice + "" +
                ", properties=" + properties + "" +
                ")";
        assertEquals(expectedString, flavor.toString());
    }

}
