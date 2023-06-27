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
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.junit.jupiter.api.Test;

/**
 * Test of DeployResource.
 */
class DeployResourceTest {

    private static final String resourceId = "f0dcb6ea-cbe7-4c88-9c94-a5d00e82a4f2";
    private static final String name = "resource";
    private static final DeployResourceKind kind = DeployResourceKind.VM;
    private static final Map<String, String> properties = Collections.singletonMap("key", "value");

    @Test
    void testConstructorAndGetters() {
        DeployResource resource = new DeployResource();
        resource.setResourceId(resourceId);
        resource.setName(name);
        resource.setKind(kind);
        resource.setProperties(properties);

        assertEquals(resourceId, resource.getResourceId());
        assertEquals(name, resource.getName());
        assertEquals(kind, resource.getKind());
        assertEquals(properties, resource.getProperties());
    }

    @Test
    void testEqualsAndHashCode() {
        DeployResource resource1 = new DeployResource();
        resource1.setResourceId(resourceId);
        resource1.setName(name);
        resource1.setKind(kind);
        resource1.setProperties(properties);

        DeployResource resource2 = new DeployResource();
        resource2.setResourceId(resourceId);
        resource2.setName(name);
        resource2.setKind(kind);
        resource2.setProperties(properties);

        DeployResource resource3 = new DeployResource();
        resource3.setResourceId("20424910-5f64-4984-84f0-6013c63c64f5");
        resource3.setName("kafka");
        resource3.setKind(DeployResourceKind.VPC);
        resource3.setProperties(Collections.singletonMap("key2", "value2"));

        assertEquals(resource1, resource1);
        assertEquals(resource1, resource2);
        assertNotEquals(resource1, resource3);

        assertEquals(resource1.hashCode(), resource1.hashCode());
        assertEquals(resource1.hashCode(), resource2.hashCode());
        assertNotEquals(resource1.hashCode(), resource3.hashCode());
    }

    @Test
    void testToString() {
        DeployResource resource = new DeployResource();
        resource.setResourceId(resourceId);
        resource.setName(name);
        resource.setKind(kind);
        resource.setProperties(properties);

        String expectedToString = "DeployResource(" +
                "resourceId=" + resourceId +
                ", name=" + name +
                ", kind=" + kind +
                ", properties=" + properties +
                ')';
        assertEquals(expectedToString, resource.toString());
    }

}
