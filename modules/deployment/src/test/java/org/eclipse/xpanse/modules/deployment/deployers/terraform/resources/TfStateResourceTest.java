/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of TfStateResource.
 */
class TfStateResourceTest {

    private static final String type = "type";
    private static final String name = "name";
    private static final String mode = "mode";
    private static TfStateResource tfStateResource;
    private static List<TfStateResourceInstance> instances;

    @BeforeEach
    void setUp() {
        TfStateResourceInstance instance = new TfStateResourceInstance();
        instance.setAttributes(Map.of("key", "value"));
        instances = List.of(instance);

        tfStateResource = new TfStateResource();
        tfStateResource.setType(type);
        tfStateResource.setName(name);
        tfStateResource.setMode(mode);
        tfStateResource.setInstances(instances);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(type, tfStateResource.getType());
        assertEquals(name, tfStateResource.getName());
        assertEquals(mode, tfStateResource.getMode());
        assertEquals(instances, tfStateResource.getInstances());
    }

    @Test
    public void testEqualsAndHashCode() {
        assertEquals(tfStateResource, tfStateResource);
        assertEquals(tfStateResource.hashCode(), tfStateResource.hashCode());

        Object obj = new Object();
        assertNotEquals(tfStateResource, obj);
        assertNotEquals(tfStateResource, null);
        assertNotEquals(tfStateResource.hashCode(), obj.hashCode());

        TfStateResource tfStateResource1 = new TfStateResource();
        TfStateResource tfStateResource2 = new TfStateResource();
        assertNotEquals(tfStateResource, tfStateResource1);
        assertNotEquals(tfStateResource, tfStateResource2);
        assertEquals(tfStateResource1, tfStateResource2);
        assertNotEquals(tfStateResource.hashCode(), tfStateResource1.hashCode());
        assertNotEquals(tfStateResource.hashCode(), tfStateResource2.hashCode());
        assertEquals(tfStateResource1.hashCode(), tfStateResource2.hashCode());

        tfStateResource1.setType(type);
        assertNotEquals(tfStateResource, tfStateResource1);
        assertNotEquals(tfStateResource1, tfStateResource2);
        assertNotEquals(tfStateResource.hashCode(), tfStateResource1.hashCode());
        assertNotEquals(tfStateResource1.hashCode(), tfStateResource2.hashCode());

        tfStateResource1.setName(name);
        assertNotEquals(tfStateResource, tfStateResource1);
        assertNotEquals(tfStateResource1, tfStateResource2);
        assertNotEquals(tfStateResource.hashCode(), tfStateResource1.hashCode());
        assertNotEquals(tfStateResource1.hashCode(), tfStateResource2.hashCode());

        tfStateResource1.setMode(mode);
        assertNotEquals(tfStateResource, tfStateResource1);
        assertNotEquals(tfStateResource1, tfStateResource2);
        assertNotEquals(tfStateResource.hashCode(), tfStateResource1.hashCode());
        assertNotEquals(tfStateResource1.hashCode(), tfStateResource2.hashCode());

        tfStateResource1.setInstances(instances);
        assertEquals(tfStateResource, tfStateResource1);
        assertNotEquals(tfStateResource1, tfStateResource2);
        assertEquals(tfStateResource.hashCode(), tfStateResource1.hashCode());
        assertNotEquals(tfStateResource1.hashCode(), tfStateResource2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString = "TfStateResource(" +
                "type=" + type + ", " +
                "name=" + name + ", " +
                "mode=" + mode + ", " +
                "instances=" + instances + ")";
        assertEquals(expectedToString, tfStateResource.toString());
    }

}
