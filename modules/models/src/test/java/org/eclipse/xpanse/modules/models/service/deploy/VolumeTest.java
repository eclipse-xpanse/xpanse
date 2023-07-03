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
    private static Volume volume;

    @BeforeEach
    void setUp() {
        volume = new Volume();
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
        assertEquals(volume, volume);
        assertEquals(volume.hashCode(), volume.hashCode());

        Object obj = new Object();
        assertNotEquals(volume, obj);
        assertNotEquals(volume, null);
        assertNotEquals(volume.hashCode(), obj.hashCode());

        Volume volume1 = new Volume();
        Volume volume2 = new Volume();
        assertNotEquals(volume, volume1);
        assertNotEquals(volume, volume2);
        assertEquals(volume1, volume2);
        assertNotEquals(volume.hashCode(), volume1.hashCode());
        assertNotEquals(volume.hashCode(), volume2.hashCode());
        assertEquals(volume1.hashCode(), volume2.hashCode());

        volume1.setSize(size);
        assertNotEquals(volume, volume1);
        assertNotEquals(volume1, volume2);
        assertNotEquals(volume.hashCode(), volume1.hashCode());
        assertNotEquals(volume1.hashCode(), volume2.hashCode());

        volume1.setType(type);
        assertNotEquals(volume, volume1);
        assertNotEquals(volume1, volume2);
        assertNotEquals(volume.hashCode(), volume1.hashCode());
        assertNotEquals(volume1.hashCode(), volume2.hashCode());

        volume1.setName(name);
        assertNotEquals(volume, volume1);
        assertNotEquals(volume1, volume2);
        assertNotEquals(volume.hashCode(), volume1.hashCode());
        assertNotEquals(volume1.hashCode(), volume2.hashCode());

        volume1.setResourceId(resourceId);
        assertNotEquals(volume, volume1);
        assertNotEquals(volume1, volume2);
        assertNotEquals(volume.hashCode(), volume1.hashCode());
        assertNotEquals(volume1.hashCode(), volume2.hashCode());

        volume1.setKind(kind);
        assertNotEquals(volume, volume1);
        assertNotEquals(volume1, volume2);
        assertNotEquals(volume.hashCode(), volume1.hashCode());
        assertNotEquals(volume1.hashCode(), volume2.hashCode());

        volume1.setProperties(properties);
        assertEquals(volume, volume1);
        assertNotEquals(volume1, volume2);
        assertEquals(volume.hashCode(), volume1.hashCode());
        assertNotEquals(volume1.hashCode(), volume2.hashCode());
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
