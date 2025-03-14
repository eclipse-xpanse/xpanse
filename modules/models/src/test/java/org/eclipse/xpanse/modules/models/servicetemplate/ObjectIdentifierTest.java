/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

/** Test of ObjectIdentifier. */
public class ObjectIdentifierTest {

    private final String name = "test";

    private final String valueSchema = "valueSchema";

    private ObjectIdentifier objectIdentifier;

    @BeforeEach
    void setUp() {
        objectIdentifier = new ObjectIdentifier();
        objectIdentifier.setName(name);
        objectIdentifier.setValueSchema(valueSchema);
    }

    @Test
    void testGetters() {
        assertEquals(name, objectIdentifier.getName());
        assertEquals(valueSchema, objectIdentifier.getValueSchema());
    }

    @Test
    public void testEqualsAndHashCode() {
        ObjectIdentifier obj = new ObjectIdentifier();
        assertNotEquals(objectIdentifier, obj);
        assertNotEquals(objectIdentifier.hashCode(), obj.hashCode());

        ObjectIdentifier objectIdentifier1 = new ObjectIdentifier();
        assertNotEquals(objectIdentifier, objectIdentifier1);
        assertNotEquals(objectIdentifier.hashCode(), objectIdentifier1.hashCode());

        BeanUtils.copyProperties(objectIdentifier, objectIdentifier1);
        assertEquals(objectIdentifier, objectIdentifier1);
        assertEquals(objectIdentifier.hashCode(), objectIdentifier1.hashCode());
    }

    @Test
    void testToString() {
        String expectedString =
                "ObjectIdentifier(" + "name=" + name + ", valueSchema=" + valueSchema + ")";
        assertEquals(expectedString, objectIdentifier.toString());
    }
}
