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
 * Test of PublicIp.
 */
class PublicIpTest {

    private static final String ip = "192.168.0.1";
    private static final String resourceId =
            UUID.fromString("f0dcb6ea-cbe7-4c88-9c94-a5d00e82a4f2").toString();
    private static final String name = "public_ip";
    private static final DeployResourceKind kind = DeployResourceKind.PUBLIC_IP;
    private static final Map<String, String> properties = Collections.singletonMap("key", "value");
    private static PublicIp publicIp;

    @BeforeEach
    void setUp() {
        publicIp = new PublicIp();
        publicIp.setIp(ip);
        publicIp.setResourceId(resourceId);
        publicIp.setName(name);
        publicIp.setKind(kind);
        publicIp.setProperties(properties);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals(ip, publicIp.getIp());
        assertEquals(resourceId, publicIp.getResourceId());
        assertEquals(name, publicIp.getName());
        assertEquals(kind, publicIp.getKind());
        assertEquals(properties, publicIp.getProperties());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(publicIp, publicIp);
        assertEquals(publicIp.hashCode(), publicIp.hashCode());

        Object obj = new Object();
        assertNotEquals(publicIp, obj);
        assertNotEquals(publicIp, null);
        assertNotEquals(publicIp.hashCode(), obj.hashCode());

        PublicIp publicIp1 = new PublicIp();
        PublicIp publicIp2 = new PublicIp();
        assertNotEquals(publicIp, publicIp1);
        assertNotEquals(publicIp, publicIp2);
        assertEquals(publicIp1, publicIp2);
        assertNotEquals(publicIp.hashCode(), publicIp1.hashCode());
        assertNotEquals(publicIp.hashCode(), publicIp2.hashCode());
        assertEquals(publicIp1.hashCode(), publicIp2.hashCode());


        publicIp1.setIp(ip);
        assertNotEquals(publicIp, publicIp1);
        assertNotEquals(publicIp1, publicIp2);
        assertNotEquals(publicIp.hashCode(), publicIp1.hashCode());
        assertNotEquals(publicIp1.hashCode(), publicIp2.hashCode());

        publicIp1.setResourceId(resourceId);
        assertNotEquals(publicIp, publicIp1);
        assertNotEquals(publicIp1, publicIp2);
        assertNotEquals(publicIp.hashCode(), publicIp1.hashCode());
        assertNotEquals(publicIp1.hashCode(), publicIp2.hashCode());

        publicIp1.setName(name);
        assertNotEquals(publicIp, publicIp1);
        assertNotEquals(publicIp1, publicIp2);
        assertNotEquals(publicIp.hashCode(), publicIp1.hashCode());
        assertNotEquals(publicIp1.hashCode(), publicIp2.hashCode());

        publicIp1.setKind(kind);
        assertNotEquals(publicIp, publicIp1);
        assertNotEquals(publicIp1, publicIp2);
        assertNotEquals(publicIp.hashCode(), publicIp1.hashCode());
        assertNotEquals(publicIp1.hashCode(), publicIp2.hashCode());

        publicIp1.setProperties(properties);
        assertEquals(publicIp, publicIp1);
        assertNotEquals(publicIp1, publicIp2);
        assertEquals(publicIp.hashCode(), publicIp1.hashCode());
        assertNotEquals(publicIp1.hashCode(), publicIp2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString =
                "PublicIp(super=DeployResource(resourceId=" + resourceId + ", name=" + name +
                        ", kind=" + kind + ", properties=" + properties + "), ip=" + ip + ")";
        assertEquals(expectedToString, publicIp.toString());
    }

}
