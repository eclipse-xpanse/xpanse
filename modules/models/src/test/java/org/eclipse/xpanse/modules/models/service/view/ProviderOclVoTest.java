/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import jakarta.validation.Valid;
import java.util.List;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.register.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of ProviderOclVo.
 */
class ProviderOclVoTest {

    private static final Csp cspName = Csp.AWS;
    private static List<@Valid Region> regions;
    private static List<@Valid UserAvailableServiceVo> details;
    private static ProviderOclVo providerOclVo;

    @BeforeEach
    void setUp() {
        Region region = new Region();
        region.setName("cn-north-1");
        region.setArea("Asia");
        regions = List.of(region);

        UserAvailableServiceVo userAvailableServiceVo = new UserAvailableServiceVo();
        userAvailableServiceVo.setName("kafka");
        details = List.of(userAvailableServiceVo);

        providerOclVo = new ProviderOclVo();
        providerOclVo.setName(cspName);
        providerOclVo.setRegions(regions);
        providerOclVo.setDetails(details);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(cspName, providerOclVo.getName());
        assertEquals(regions, providerOclVo.getRegions());
        assertEquals(details, providerOclVo.getDetails());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(providerOclVo, providerOclVo);
        assertEquals(providerOclVo.hashCode(), providerOclVo.hashCode());

        Object obj = new Object();
        assertNotEquals(providerOclVo, obj);
        assertNotEquals(providerOclVo, null);
        assertNotEquals(providerOclVo.hashCode(), obj.hashCode());

        ProviderOclVo providerOclVo1 = new ProviderOclVo();
        ProviderOclVo providerOclVo2 = new ProviderOclVo();
        assertNotEquals(providerOclVo, providerOclVo1);
        assertNotEquals(providerOclVo, providerOclVo2);
        assertEquals(providerOclVo1, providerOclVo2);
        assertNotEquals(providerOclVo.hashCode(), providerOclVo1.hashCode());
        assertNotEquals(providerOclVo.hashCode(), providerOclVo2.hashCode());
        assertEquals(providerOclVo1.hashCode(), providerOclVo2.hashCode());

        providerOclVo1.setName(cspName);
        assertNotEquals(providerOclVo, providerOclVo1);
        assertNotEquals(providerOclVo1, providerOclVo2);
        assertNotEquals(providerOclVo.hashCode(), providerOclVo1.hashCode());
        assertNotEquals(providerOclVo1.hashCode(), providerOclVo2.hashCode());

        providerOclVo1.setRegions(regions);
        assertNotEquals(providerOclVo, providerOclVo1);
        assertNotEquals(providerOclVo1, providerOclVo2);
        assertNotEquals(providerOclVo.hashCode(), providerOclVo1.hashCode());
        assertNotEquals(providerOclVo1.hashCode(), providerOclVo2.hashCode());

        providerOclVo1.setDetails(details);
        assertEquals(providerOclVo, providerOclVo1);
        assertNotEquals(providerOclVo1, providerOclVo2);
        assertEquals(providerOclVo.hashCode(), providerOclVo1.hashCode());
        assertNotEquals(providerOclVo1.hashCode(), providerOclVo2.hashCode());
    }


    @Test
    void testToString() {
        String expectedToString = "ProviderOclVo(" +
                "name=" + cspName + ", " +
                "regions=" + regions + ", " +
                "details=" + details + ")";
        assertEquals(expectedToString, providerOclVo.toString());
    }

}
