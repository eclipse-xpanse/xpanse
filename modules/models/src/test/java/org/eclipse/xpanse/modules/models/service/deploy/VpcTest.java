/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.deploy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of Vpc.
 */
class VpcTest {

    private static final String vpcStr = "192.168.0.0/16";
    private static final String subnetStr = "192.168.10.0/24";
    private static final String resourceId =
            UUID.fromString("f0dcb6ea-cbe7-4c88-9c94-a5d00e82a4f2").toString();
    private static final String name = "vpc";
    private static final DeployResourceKind kind = DeployResourceKind.VPC;
    private static final Map<String, String> properties = Collections.singletonMap("key", "value");
    private static final Vpc vpc = new Vpc();

    @BeforeEach
    void setUp() {
        vpc.setVpc(vpcStr);
        vpc.setSubnet(subnetStr);
        vpc.setResourceId(resourceId);
        vpc.setName(name);
        vpc.setKind(kind);
        vpc.setProperties(properties);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals(vpcStr, vpc.getVpc());
        assertEquals(subnetStr, vpc.getSubnet());
        assertEquals(resourceId, vpc.getResourceId());
        assertEquals(name, vpc.getName());
        assertEquals(kind, vpc.getKind());
        assertEquals(properties, vpc.getProperties());
    }

    @Test
    void testEqualsAndHashCode() {
        Vpc vpc2 = new Vpc();
        vpc2.setVpc(vpcStr);
        vpc2.setSubnet(subnetStr);
        vpc2.setResourceId(resourceId);
        vpc2.setName(name);
        vpc2.setKind(kind);
        vpc2.setProperties(properties);

        Vpc vpc3 = new Vpc();
        vpc3.setVpc("192.168.1.1/16");
        vpc3.setSubnet("192.168.10.10/24");
        vpc3.setResourceId("20424910-5f64-4984-84f0-6013c63c64f5");
        vpc3.setName(name);
        vpc3.setKind(DeployResourceKind.VPC);
        vpc3.setProperties(properties);

        assertEquals(vpc, vpc);
        assertEquals(vpc, vpc2);
        assertNotEquals(vpc, vpc3);

        assertEquals(vpc.hashCode(), vpc.hashCode());
        assertEquals(vpc.hashCode(), vpc2.hashCode());
        assertNotEquals(vpc.hashCode(), vpc3.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString =
                "Vpc(super=DeployResource(resourceId=" + resourceId + ", name=" + name + ", kind=" +
                        kind + ", properties=" + properties + "), vpc=" + vpcStr + ", subnet=" +
                        subnetStr + ")";
        assertEquals(expectedToString, vpc.toString());
    }
}
