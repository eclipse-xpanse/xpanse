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
import org.springframework.beans.BeanUtils;

/**
 * Test of Region.
 */
class RegionTest {

    private final String name = "cn-north-1";
    private final String site = "default";
    private final String area = "Asia";
    private Region region;

    @BeforeEach
    void setUp() {
        region = new Region();
        region.setName(name);
        region.setSite(site);
        region.setArea(area);
    }

    @Test
    void testGetters() {
        assertEquals(name, region.getName());
        assertEquals(site, region.getSite());
        assertEquals(area, region.getArea());

        Region region1 = new Region();
        assertEquals("Others", region1.getArea());
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertNotEquals(region, obj);
        assertNotEquals(region.hashCode(), obj.hashCode());

        Region region1 = new Region();
        assertNotEquals(region, region1);
        assertNotEquals(region.hashCode(), region1.hashCode());

        BeanUtils.copyProperties(region, region1);
        assertEquals(region, region1);
        assertEquals(region.hashCode(), region1.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "Region(name=" + name + ", site=" + site + ", area=" + area + ")";
        assertEquals(expectedString, region.toString());
    }

}
