/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

/**
 * Test of FlavorBasic.
 */
class ServiceFlavorTest {

    private final String name = "flavor";
    private final int priority = 1;
    private final Map<String, String> properties = Map.of("key", "value");
    private ServiceFlavor serviceFlavor;


    @BeforeEach
    void setUp() {
        serviceFlavor = new ServiceFlavor();
        serviceFlavor.setName(name);
        serviceFlavor.setProperties(properties);
        serviceFlavor.setPriority(priority);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(name, serviceFlavor.getName());
        assertEquals(properties, serviceFlavor.getProperties());
        assertEquals(priority, serviceFlavor.getPriority());
    }

    @Test
    void testEqualsAndHashCode() {

        Object obj = new Object();
        assertFalse(serviceFlavor.canEqual(obj));
        assertNotEquals(serviceFlavor, obj);
        assertNotEquals(serviceFlavor.hashCode(), obj.hashCode());

        ServiceFlavor test1 = new ServiceFlavor();
        assertTrue(serviceFlavor.canEqual(test1));
        assertNotEquals(serviceFlavor, test1);
        assertNotEquals(serviceFlavor.hashCode(), test1.hashCode());

        BeanUtils.copyProperties(serviceFlavor, test1);
        assertTrue(serviceFlavor.canEqual(test1));
        assertEquals(serviceFlavor, test1);
        assertEquals(serviceFlavor.hashCode(), test1.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "ServiceFlavor(name=" + name
                + ", properties=" + properties
                + ", priority=" + priority + ")";
        assertEquals(expectedString, serviceFlavor.toString());
    }

}
