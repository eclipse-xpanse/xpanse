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
    private static final PublicIp publicIp = new PublicIp();

    @BeforeEach
    void setUp() {
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
        PublicIp publicIp2 = new PublicIp();
        publicIp2.setIp(ip);
        publicIp2.setResourceId(resourceId);
        publicIp2.setName(name);
        publicIp2.setKind(kind);
        publicIp2.setProperties(properties);

        PublicIp publicIp3 = new PublicIp();
        publicIp3.setIp("192.168.0.0");
        publicIp3.setResourceId("20424910-5f64-4984-84f0-6013c63c64f5");
        publicIp3.setName("name");
        publicIp3.setKind(DeployResourceKind.PUBLIC_IP);
        publicIp3.setProperties(properties);

        assertEquals(publicIp, publicIp);
        assertEquals(publicIp, publicIp2);
        assertNotEquals(publicIp, publicIp3);

        assertEquals(publicIp.hashCode(), publicIp.hashCode());
        assertEquals(publicIp.hashCode(), publicIp2.hashCode());
        assertNotEquals(publicIp.hashCode(), publicIp3.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString =
                "PublicIp(super=DeployResource(resourceId=" + resourceId + ", name=" + name +
                        ", kind=" + kind + ", properties=" + properties + "), ip=" + ip + ")";
        assertEquals(expectedToString, publicIp.toString());
    }

}
