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
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

/** Test of ObjectHandlerScript. */
public class ObjectHandlerScriptTest {

    private final Boolean runOnlyOnce = Boolean.TRUE;
    @Mock private AnsibleScriptConfig ansibleScriptConfig;

    private ObjectHandlerScript objectHandlerScript;

    @BeforeEach
    void setUp() {
        objectHandlerScript = new ObjectHandlerScript();
        objectHandlerScript.setRunOnlyOnce(runOnlyOnce);
        objectHandlerScript.setAnsibleScriptConfig(ansibleScriptConfig);
    }

    @Test
    void testGetters() {
        assertEquals(runOnlyOnce, objectHandlerScript.getRunOnlyOnce());
        assertEquals(ansibleScriptConfig, objectHandlerScript.getAnsibleScriptConfig());
    }

    @Test
    public void testEqualsAndHashCode() {
        ObjectHandlerScript obj = new ObjectHandlerScript();
        assertNotEquals(objectHandlerScript, obj);
        assertNotEquals(objectHandlerScript.hashCode(), obj.hashCode());

        ObjectHandlerScript objectHandlerScript1 = new ObjectHandlerScript();
        assertNotEquals(objectHandlerScript, objectHandlerScript1);
        assertNotEquals(objectHandlerScript.hashCode(), objectHandlerScript1.hashCode());

        BeanUtils.copyProperties(objectHandlerScript, objectHandlerScript1);
        assertEquals(objectHandlerScript, objectHandlerScript1);
        assertEquals(objectHandlerScript.hashCode(), objectHandlerScript1.hashCode());
    }

    @Test
    void testToString() {
        String expectedString =
                "ObjectHandlerScript("
                        + "runOnlyOnce="
                        + runOnlyOnce
                        + ", ansibleScriptConfig="
                        + ansibleScriptConfig
                        + ")";
        assertEquals(expectedString, objectHandlerScript.toString());
    }
}
