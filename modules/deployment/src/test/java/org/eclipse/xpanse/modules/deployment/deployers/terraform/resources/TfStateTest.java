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
 * Test of TfState.
 */
class TfStateTest {

    private static TfState tfState;

    private static Map<String, TfOutput> outputs;

    private static List<TfStateResource> resources;

    @BeforeEach
    void setUp() {
        TfOutput tfOutput = new TfOutput();
        tfOutput.setType("type");
        tfOutput.setValue("value");
        outputs = Map.of("key", tfOutput);

        TfStateResource tfStateResource = new TfStateResource();
        tfStateResource.setType("type");
        tfStateResource.setName("name");
        resources = List.of(tfStateResource);

        tfState = new TfState();
        tfState.setOutputs(outputs);
        tfState.setResources(resources);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(outputs, tfState.getOutputs());
        assertEquals(resources, tfState.getResources());
    }

    @Test
    public void testEqualsAndHashCode() {
        assertEquals(tfState, tfState);
        assertEquals(tfState.hashCode(), tfState.hashCode());

        Object obj = new Object();
        assertNotEquals(tfState, obj);
        assertNotEquals(tfState, null);
        assertNotEquals(tfState.hashCode(), obj.hashCode());

        TfState tfState1 = new TfState();
        TfState tfState2 = new TfState();
        assertNotEquals(tfState, tfState1);
        assertNotEquals(tfState, tfState2);
        assertEquals(tfState1, tfState2);
        assertNotEquals(tfState.hashCode(), tfState1.hashCode());
        assertNotEquals(tfState.hashCode(), tfState2.hashCode());
        assertEquals(tfState1.hashCode(), tfState2.hashCode());

        tfState1.setOutputs(outputs);
        assertNotEquals(tfState, tfState1);
        assertNotEquals(tfState1, tfState2);
        assertNotEquals(tfState.hashCode(), tfState1.hashCode());
        assertNotEquals(tfState1.hashCode(), tfState2.hashCode());

        tfState1.setResources(resources);
        assertEquals(tfState, tfState1);
        assertNotEquals(tfState1, tfState2);
        assertEquals(tfState.hashCode(), tfState1.hashCode());
        assertNotEquals(tfState1.hashCode(), tfState2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString = "TfState(" +
                "outputs=" + outputs + ", " +
                "resources=" + resources + ")";
        assertEquals(expectedToString, tfState.toString());
    }

}
