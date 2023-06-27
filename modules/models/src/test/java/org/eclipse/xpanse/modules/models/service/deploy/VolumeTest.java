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
 * Test of Volume.
 */
class VolumeTest {

    private static final String size = "100G";
    private static final String type = "SSD";
    private static final String resourceId =
            UUID.fromString("f0dcb6ea-cbe7-4c88-9c94-a5d00e82a4f2").toString();
    private static final String name = "volume";
    private static final DeployResourceKind kind = DeployResourceKind.VOLUME;
    private static final Map<String, String> properties = Collections.singletonMap("key", "value");
    private static final Volume volume = new Volume();

    @BeforeEach
    void setUp() {
        volume.setSize(size);
        volume.setType(type);
        volume.setResourceId(resourceId);
        volume.setName(name);
        volume.setKind(kind);
        volume.setProperties(properties);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals(size, volume.getSize());
        assertEquals(type, volume.getType());
        assertEquals(resourceId, volume.getResourceId());
        assertEquals(name, volume.getName());
        assertEquals(kind, volume.getKind());
        assertEquals(properties, volume.getProperties());
    }

    @Test
    void testEqualsAndHashCode() {
        Volume volume2 = new Volume();
        volume2.setSize(size);
        volume2.setType(type);
        volume2.setResourceId(resourceId);
        volume2.setName(name);
        volume2.setKind(kind);
        volume2.setProperties(properties);

        Volume volume3 = new Volume();
        volume3.setSize("300G");
        volume3.setType("HDD");
        volume3.setResourceId("20424910-5f64-4984-84f0-6013c63c64f5");
        volume3.setName(name);
        volume3.setKind(DeployResourceKind.VOLUME);
        volume3.setProperties(properties);

        assertEquals(volume, volume);
        assertEquals(volume, volume2);
        assertNotEquals(volume, volume3);

        assertEquals(volume.hashCode(), volume.hashCode());
        assertEquals(volume.hashCode(), volume2.hashCode());
        assertNotEquals(volume.hashCode(), volume3.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString =
                "Volume(super=DeployResource(resourceId=" + resourceId + ", name=" + name +
                        ", kind=" + kind + ", properties=" + properties + "), size=" + size +
                        ", type=" + type + ")";
        assertEquals(expectedToString, volume.toString());
    }

}
