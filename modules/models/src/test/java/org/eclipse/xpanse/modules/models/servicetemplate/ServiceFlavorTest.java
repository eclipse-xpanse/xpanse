/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of FlavorBasic.
 */
class ServiceFlavorTest {

    private static final String name = "flavor";
    private static final Integer fixedPrice = 1;
    private static final Map<String, String> properties = Map.of("key", "value");
    private static ServiceFlavor serviceFlavor;

    @BeforeEach
    void setUp() {
        serviceFlavor = new ServiceFlavor();
        serviceFlavor.setName(name);
        serviceFlavor.setFixedPrice(fixedPrice);
        serviceFlavor.setProperties(properties);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(name, serviceFlavor.getName());
        assertEquals(fixedPrice, serviceFlavor.getFixedPrice());
        assertEquals(properties, serviceFlavor.getProperties());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(serviceFlavor, serviceFlavor);
        assertEquals(serviceFlavor.hashCode(), serviceFlavor.hashCode());

        Object obj = new Object();
        assertNotEquals(serviceFlavor, obj);
        assertNotEquals(serviceFlavor, null);
        assertNotEquals(serviceFlavor.hashCode(), obj.hashCode());

        ServiceFlavor serviceFlavor1 = new ServiceFlavor();
        ServiceFlavor serviceFlavor2 = new ServiceFlavor();
        assertNotEquals(serviceFlavor, serviceFlavor1);
        assertNotEquals(serviceFlavor, serviceFlavor2);
        assertEquals(serviceFlavor1, serviceFlavor2);
        assertNotEquals(serviceFlavor.hashCode(), serviceFlavor1.hashCode());
        assertNotEquals(serviceFlavor.hashCode(), serviceFlavor2.hashCode());
        assertEquals(serviceFlavor1.hashCode(), serviceFlavor2.hashCode());

        serviceFlavor1.setName(name);
        assertNotEquals(serviceFlavor, serviceFlavor1);
        assertNotEquals(serviceFlavor1, serviceFlavor2);
        assertNotEquals(serviceFlavor.hashCode(), serviceFlavor1.hashCode());
        assertNotEquals(serviceFlavor1.hashCode(), serviceFlavor2.hashCode());

        serviceFlavor1.setFixedPrice(fixedPrice);
        serviceFlavor1.setProperties(properties);
        assertEquals(serviceFlavor, serviceFlavor1);
        assertNotEquals(serviceFlavor1, serviceFlavor2);
        assertEquals(serviceFlavor.hashCode(), serviceFlavor1.hashCode());
        assertNotEquals(serviceFlavor1.hashCode(), serviceFlavor2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "ServiceFlavor(" +
                "name=" + name +
                ", fixedPrice=" + fixedPrice +
                ", properties=" + properties + ")";
        assertEquals(expectedString, serviceFlavor.toString());
    }

}
