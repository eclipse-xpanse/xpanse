/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ObjectActionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

/** Test of ObjectManage. */
public class ObjectManageTest {

    private final ObjectActionType objectActionType = ObjectActionType.CREATE;

    @Mock private ModificationImpact modificationImpact;
    @Mock private ServiceChangeScript objectHandlerScript;
    @Mock private List<ObjectParameter> objectParameters;

    private ObjectManage objectManage;

    @BeforeEach
    void setUp() {
        objectManage = new ObjectManage();
        objectManage.setObjectActionType(objectActionType);
        objectManage.setModificationImpact(modificationImpact);
        objectManage.setObjectHandlerScript(objectHandlerScript);
        objectManage.setObjectParameters(objectParameters);
    }

    @Test
    void testGetters() {
        assertEquals(objectActionType, objectManage.getObjectActionType());
        assertEquals(modificationImpact, objectManage.getModificationImpact());
        assertEquals(objectHandlerScript, objectManage.getObjectHandlerScript());
        assertEquals(objectParameters, objectManage.getObjectParameters());
    }

    @Test
    public void testEqualsAndHashCode() {
        ObjectManage obj = new ObjectManage();
        assertNotEquals(objectManage, obj);
        assertNotEquals(objectManage.hashCode(), obj.hashCode());

        ObjectManage objectManage1 = new ObjectManage();
        assertNotEquals(objectManage, objectManage1);
        assertNotEquals(objectManage.hashCode(), objectManage1.hashCode());

        BeanUtils.copyProperties(objectManage, objectManage1);
        assertEquals(objectManage, objectManage1);
        assertEquals(objectManage.hashCode(), objectManage1.hashCode());
    }

    @Test
    void testToString() {
        String expectedString =
                "ObjectManage("
                        + "objectActionType="
                        + objectActionType
                        + ", modificationImpact="
                        + modificationImpact
                        + ", objectHandlerScript="
                        + objectHandlerScript
                        + ", objectParameters="
                        + objectParameters
                        + ", controllerApiMethods="
                        + null
                        + ")";
        assertEquals(expectedString, objectManage.toString());
    }
}
