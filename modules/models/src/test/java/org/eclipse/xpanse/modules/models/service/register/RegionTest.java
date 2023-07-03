/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.register;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of Region.
 */
class RegionTest {

    private static final String name = "cn-north-1";
    private static final String area = "Area";
    private static Region region;

    @BeforeEach
    void setUp() {
        region = new Region();
        region.setName(name);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(name, region.getName());
        assertEquals("Others", region.getArea());

        region.setArea(area);

        assertEquals(area, region.getArea());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(region, region);
        assertEquals(region.hashCode(), region.hashCode());

        Object obj = new Object();
        assertNotEquals(region, obj);
        assertNotEquals(region, null);
        assertNotEquals(region.hashCode(), obj.hashCode());

        Region region1 = new Region();
        Region region2 = new Region();
        assertNotEquals(region, region1);
        assertNotEquals(region, region2);
        assertEquals(region1, region2);
        assertNotEquals(region.hashCode(), region1.hashCode());
        assertNotEquals(region.hashCode(), region2.hashCode());
        assertEquals(region1.hashCode(), region2.hashCode());

        region1.setName(name);
        assertEquals(region, region1);
        assertNotEquals(region1, region2);
        assertEquals(region.hashCode(), region1.hashCode());
        assertNotEquals(region1.hashCode(), region2.hashCode());

        region1.setArea(area);
        assertNotEquals(region, region1);
        assertNotEquals(region1, region2);
        assertNotEquals(region.hashCode(), region1.hashCode());
        assertNotEquals(region1.hashCode(), region2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "Region(" +
                "name=" + name +
                ", area=Others)";
        assertEquals(expectedString, region.toString());
    }

}
