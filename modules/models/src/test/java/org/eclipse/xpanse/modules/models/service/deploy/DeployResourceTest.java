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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of DeployResource.
 */
class DeployResourceTest {

    private static final String resourceId = "f0dcb6ea-cbe7-4c88-9c94-a5d00e82a4f2";
    private static final String name = "resource";
    private static final DeployResourceKind kind = DeployResourceKind.VM;
    private static final Map<String, String> properties = Collections.singletonMap("key", "value");
    private static DeployResource resource;

    @BeforeEach
    void setUp() {
        resource = new DeployResource();
        resource.setResourceId(resourceId);
        resource.setName(name);
        resource.setKind(kind);
        resource.setProperties(properties);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals(resourceId, resource.getResourceId());
        assertEquals(name, resource.getName());
        assertEquals(kind, resource.getKind());
        assertEquals(properties, resource.getProperties());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(resource, resource);
        assertEquals(resource.hashCode(), resource.hashCode());

        Object obj = new Object();
        assertNotEquals(resource, obj);
        assertNotEquals(resource, null);
        assertNotEquals(resource.hashCode(), obj.hashCode());

        DeployResource resource1 = new DeployResource();
        DeployResource resource2 = new DeployResource();
        assertNotEquals(resource, resource1);
        assertNotEquals(resource, resource2);
        assertEquals(resource1, resource2);
        assertNotEquals(resource.hashCode(), resource1.hashCode());
        assertNotEquals(resource.hashCode(), resource2.hashCode());
        assertEquals(resource1.hashCode(), resource2.hashCode());

        resource1.setResourceId(resourceId);
        assertNotEquals(resource, resource1);
        assertNotEquals(resource1, resource2);
        assertNotEquals(resource.hashCode(), resource1.hashCode());
        assertNotEquals(resource1.hashCode(), resource2.hashCode());

        resource1.setName(name);
        assertNotEquals(resource, resource1);
        assertNotEquals(resource1, resource2);
        assertNotEquals(resource.hashCode(), resource1.hashCode());
        assertNotEquals(resource1.hashCode(), resource2.hashCode());

        resource1.setKind(kind);
        assertNotEquals(resource, resource1);
        assertNotEquals(resource1, resource2);
        assertNotEquals(resource.hashCode(), resource1.hashCode());
        assertNotEquals(resource1.hashCode(), resource2.hashCode());

        resource1.setProperties(properties);
        assertEquals(resource, resource1);
        assertNotEquals(resource1, resource2);
        assertEquals(resource.hashCode(), resource1.hashCode());
        assertNotEquals(resource1.hashCode(), resource2.hashCode());
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
