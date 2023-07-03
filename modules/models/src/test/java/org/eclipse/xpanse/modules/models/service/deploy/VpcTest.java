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
    private static Vpc vpc;

    @BeforeEach
    void setUp() {
        vpc = new Vpc();
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
        assertEquals(vpc, vpc);
        assertEquals(vpc.hashCode(), vpc.hashCode());

        Object obj = new Object();
        assertNotEquals(vpc, obj);
        assertNotEquals(vpc, null);
        assertNotEquals(vpc.hashCode(), obj.hashCode());

        Vpc vpc1 = new Vpc();
        Vpc vpc2 = new Vpc();
        assertNotEquals(vpc, vpc1);
        assertNotEquals(vpc, vpc2);
        assertEquals(vpc1, vpc2);
        assertNotEquals(vpc.hashCode(), vpc1.hashCode());
        assertNotEquals(vpc.hashCode(), vpc2.hashCode());
        assertEquals(vpc1.hashCode(), vpc2.hashCode());

        vpc1.setVpc(vpcStr);
        assertNotEquals(vpc, vpc1);
        assertNotEquals(vpc1, vpc2);
        assertNotEquals(vpc.hashCode(), vpc1.hashCode());
        assertNotEquals(vpc1.hashCode(), vpc2.hashCode());

        vpc1.setSubnet(subnetStr);
        assertNotEquals(vpc, vpc1);
        assertNotEquals(vpc1, vpc2);
        assertNotEquals(vpc.hashCode(), vpc1.hashCode());
        assertNotEquals(vpc1.hashCode(), vpc2.hashCode());

        vpc1.setResourceId(resourceId);
        assertNotEquals(vpc, vpc1);
        assertNotEquals(vpc1, vpc2);
        assertNotEquals(vpc.hashCode(), vpc1.hashCode());
        assertNotEquals(vpc1.hashCode(), vpc2.hashCode());

        vpc1.setName(name);
        assertNotEquals(vpc, vpc1);
        assertNotEquals(vpc1, vpc2);
        assertNotEquals(vpc.hashCode(), vpc1.hashCode());
        assertNotEquals(vpc1.hashCode(), vpc2.hashCode());

        vpc1.setKind(kind);
        assertNotEquals(vpc, vpc1);
        assertNotEquals(vpc1, vpc2);
        assertNotEquals(vpc.hashCode(), vpc1.hashCode());
        assertNotEquals(vpc1.hashCode(), vpc2.hashCode());

        vpc1.setProperties(properties);
        assertEquals(vpc, vpc1);
        assertNotEquals(vpc1, vpc2);
        assertEquals(vpc.hashCode(), vpc1.hashCode());
        assertNotEquals(vpc1.hashCode(), vpc2.hashCode());
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
