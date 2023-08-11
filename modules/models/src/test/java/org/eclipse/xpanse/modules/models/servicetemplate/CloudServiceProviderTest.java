/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of CloudServiceProvider.
 */
class CloudServiceProviderTest {

    private static final Csp name = Csp.HUAWEI;
    private static List<Region> regions;
    private static CloudServiceProvider cloudServiceProvider;

    @BeforeEach
    void setUp() {
        Region region = new Region();
        region.setName("cn-north-1");
        region.setArea("Area");
        regions = List.of(region);

        cloudServiceProvider = new CloudServiceProvider();
        cloudServiceProvider.setName(name);
        cloudServiceProvider.setRegions(regions);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(name, cloudServiceProvider.getName());
        assertEquals(regions, cloudServiceProvider.getRegions());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(cloudServiceProvider, cloudServiceProvider);
        assertEquals(cloudServiceProvider.hashCode(), cloudServiceProvider.hashCode());

        Object obj = new Object();
        assertNotEquals(cloudServiceProvider, obj);
        assertNotEquals(cloudServiceProvider, null);
        assertNotEquals(cloudServiceProvider.hashCode(), obj.hashCode());

        CloudServiceProvider cloudServiceProvider1 = new CloudServiceProvider();
        CloudServiceProvider cloudServiceProvider2 = new CloudServiceProvider();
        assertNotEquals(cloudServiceProvider, cloudServiceProvider1);
        assertNotEquals(cloudServiceProvider, cloudServiceProvider2);
        assertEquals(cloudServiceProvider1, cloudServiceProvider2);
        assertNotEquals(cloudServiceProvider.hashCode(), cloudServiceProvider1.hashCode());
        assertNotEquals(cloudServiceProvider.hashCode(), cloudServiceProvider2.hashCode());
        assertEquals(cloudServiceProvider1.hashCode(), cloudServiceProvider2.hashCode());

        cloudServiceProvider1.setName(name);
        assertNotEquals(cloudServiceProvider, cloudServiceProvider1);
        assertNotEquals(cloudServiceProvider1, cloudServiceProvider2);
        assertNotEquals(cloudServiceProvider.hashCode(), cloudServiceProvider1.hashCode());
        assertNotEquals(cloudServiceProvider1.hashCode(), cloudServiceProvider2.hashCode());

        cloudServiceProvider1.setRegions(regions);
        assertEquals(cloudServiceProvider, cloudServiceProvider1);
        assertNotEquals(cloudServiceProvider1, cloudServiceProvider2);
        assertEquals(cloudServiceProvider.hashCode(), cloudServiceProvider1.hashCode());
        assertNotEquals(cloudServiceProvider1.hashCode(), cloudServiceProvider2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "CloudServiceProvider(" +
                "name=" + name +
                ", regions=" + regions + "" +
                ")";

        assertEquals(expectedString, cloudServiceProvider.toString());
    }

}
