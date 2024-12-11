/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test of TfOutput. */
class TfOutputTest {

    private static final String type = "type";
    private static final String value = "value";
    private static TfOutput tfOutput;

    @BeforeEach
    void setUp() {
        tfOutput = new TfOutput();
        tfOutput.setType(type);
        tfOutput.setValue(value);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(type, tfOutput.getType());
        assertEquals(value, tfOutput.getValue());
    }

    @Test
    public void testEqualsAndHashCode() {
        assertEquals(tfOutput, tfOutput);
        assertEquals(tfOutput.hashCode(), tfOutput.hashCode());

        Object obj = new Object();
        assertNotEquals(tfOutput, obj);
        assertNotEquals(tfOutput, null);
        assertNotEquals(tfOutput.hashCode(), obj.hashCode());

        TfOutput tfOutput1 = new TfOutput();
        TfOutput tfOutput2 = new TfOutput();
        assertNotEquals(tfOutput, tfOutput1);
        assertNotEquals(tfOutput, tfOutput2);
        assertEquals(tfOutput1, tfOutput2);
        assertNotEquals(tfOutput.hashCode(), tfOutput1.hashCode());
        assertNotEquals(tfOutput.hashCode(), tfOutput2.hashCode());
        assertEquals(tfOutput1.hashCode(), tfOutput2.hashCode());

        tfOutput1.setType(type);
        assertNotEquals(tfOutput, tfOutput1);
        assertNotEquals(tfOutput1, tfOutput2);
        assertNotEquals(tfOutput.hashCode(), tfOutput1.hashCode());
        assertNotEquals(tfOutput1.hashCode(), tfOutput2.hashCode());

        tfOutput1.setValue(value);
        assertEquals(tfOutput, tfOutput1);
        assertNotEquals(tfOutput1, tfOutput2);
        assertEquals(tfOutput.hashCode(), tfOutput1.hashCode());
        assertNotEquals(tfOutput1.hashCode(), tfOutput2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString = "TfOutput(" + "type=" + type + ", " + "value=" + value + ")";
        assertEquals(expectedToString, tfOutput.toString());
    }
}
