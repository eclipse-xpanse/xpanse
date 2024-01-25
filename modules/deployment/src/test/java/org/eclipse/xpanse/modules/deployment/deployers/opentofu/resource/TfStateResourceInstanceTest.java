/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of TfStateResourceInstance.
 */
class TfStateResourceInstanceTest {

    public static Map<String, Object> attributes;
    private static TfStateResourceInstance
            tfStateResourceInstance;

    @BeforeEach
    void setUp() {
        attributes = Map.of("key", "value");

        tfStateResourceInstance = new TfStateResourceInstance();
        tfStateResourceInstance.setAttributes(attributes);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(attributes, tfStateResourceInstance.getAttributes());
    }

    @Test
    public void testEqualsAndHashCode() {
        assertEquals(tfStateResourceInstance, tfStateResourceInstance);
        assertEquals(tfStateResourceInstance.hashCode(), tfStateResourceInstance.hashCode());

        Object obj = new Object();
        assertNotEquals(tfStateResourceInstance, obj);
        assertNotEquals(tfStateResourceInstance, null);
        assertNotEquals(tfStateResourceInstance.hashCode(), obj.hashCode());

        TfStateResourceInstance
                tfStateResourceInstance1 = new TfStateResourceInstance();
        TfStateResourceInstance
                tfStateResourceInstance2 = new TfStateResourceInstance();
        assertNotEquals(tfStateResourceInstance, tfStateResourceInstance1);
        assertNotEquals(tfStateResourceInstance, tfStateResourceInstance2);
        assertEquals(tfStateResourceInstance1, tfStateResourceInstance2);
        assertNotEquals(tfStateResourceInstance.hashCode(), tfStateResourceInstance1.hashCode());
        assertNotEquals(tfStateResourceInstance.hashCode(), tfStateResourceInstance2.hashCode());
        assertEquals(tfStateResourceInstance1.hashCode(), tfStateResourceInstance2.hashCode());

        tfStateResourceInstance1.setAttributes(attributes);
        assertEquals(tfStateResourceInstance, tfStateResourceInstance1);
        assertNotEquals(tfStateResourceInstance1, tfStateResourceInstance2);
        assertEquals(tfStateResourceInstance.hashCode(), tfStateResourceInstance1.hashCode());
        assertNotEquals(tfStateResourceInstance1.hashCode(), tfStateResourceInstance2.hashCode());
    }

}
