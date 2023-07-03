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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of VersionOclVo.
 */
class VersionOclVoTest {

    private static final String version = "v1.0.0";
    private static List<@Valid ProviderOclVo> cloudProvider;
    private static VersionOclVo versionOclVo;

    @BeforeEach
    void setUp() {
        ProviderOclVo providerOclVo = new ProviderOclVo();
        providerOclVo.setName(Csp.AWS);
        cloudProvider = List.of(providerOclVo);

        versionOclVo = new VersionOclVo();
        versionOclVo.setVersion(version);
        versionOclVo.setCloudProvider(cloudProvider);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(version, versionOclVo.getVersion());
        assertEquals(cloudProvider, versionOclVo.getCloudProvider());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(versionOclVo, versionOclVo);
        assertEquals(versionOclVo.hashCode(), versionOclVo.hashCode());

        Object obj = new Object();
        assertNotEquals(versionOclVo, obj);
        assertNotEquals(versionOclVo, null);
        assertNotEquals(versionOclVo.hashCode(), obj.hashCode());

        VersionOclVo versionOclVo1 = new VersionOclVo();
        VersionOclVo versionOclVo2 = new VersionOclVo();
        assertNotEquals(versionOclVo, versionOclVo1);
        assertNotEquals(versionOclVo, versionOclVo2);
        assertEquals(versionOclVo1, versionOclVo2);
        assertNotEquals(versionOclVo.hashCode(), versionOclVo1.hashCode());
        assertNotEquals(versionOclVo.hashCode(), versionOclVo2.hashCode());
        assertEquals(versionOclVo1.hashCode(), versionOclVo2.hashCode());

        versionOclVo1.setVersion(version);
        assertNotEquals(versionOclVo, versionOclVo1);
        assertNotEquals(versionOclVo1, versionOclVo2);
        assertNotEquals(versionOclVo.hashCode(), versionOclVo1.hashCode());
        assertNotEquals(versionOclVo1.hashCode(), versionOclVo2.hashCode());

        versionOclVo1.setCloudProvider(cloudProvider);
        assertEquals(versionOclVo, versionOclVo1);
        assertNotEquals(versionOclVo1, versionOclVo2);
        assertEquals(versionOclVo.hashCode(), versionOclVo1.hashCode());
        assertNotEquals(versionOclVo1.hashCode(), versionOclVo2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString = "VersionOclVo(" +
                "version=" + version + ", " +
                "cloudProvider=" + cloudProvider + ")";
        assertEquals(expectedToString, versionOclVo.toString());
    }

}
