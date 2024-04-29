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
import org.eclipse.xpanse.modules.models.billing.RatingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

/**
 * Test of F.
 */
class ServiceFlavorWithPriceTest {

    private final String name = "flavor";
    private final Map<String, String> properties = Map.of("key", "value");
    @Mock
    private RatingMode pricing;
    private ServiceFlavorWithPrice serviceFlavor;


    @BeforeEach
    void setUp() {
        serviceFlavor = new ServiceFlavorWithPrice();
        serviceFlavor.setName(name);
        serviceFlavor.setPricing(pricing);
        serviceFlavor.setProperties(properties);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(name, serviceFlavor.getName());
        assertEquals(pricing, serviceFlavor.getPricing());
        assertEquals(properties, serviceFlavor.getProperties());
    }

    @Test
    void testEqualsAndHashCode() {

        Object obj = new Object();
        assertFalse(serviceFlavor.canEqual(obj));
        assertNotEquals(serviceFlavor, obj);
        assertNotEquals(serviceFlavor.hashCode(), obj.hashCode());

        ServiceFlavorWithPrice test1 = new ServiceFlavorWithPrice();
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
        String expectedString = "ServiceFlavorWithPrice(super=ServiceFlavor(" +
                "name=" + name +
                ", properties=" + properties + "), pricing=" + pricing + ")";
        assertEquals(expectedString, serviceFlavor.toString());
    }

}
