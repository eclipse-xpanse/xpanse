/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ConfigurationManagerTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

/** Test of ServiceObject. */
public class ServiceObjectTest {

    private final String name = "uaser";
    private final ConfigurationManagerTool handlerType = ConfigurationManagerTool.ANSIBLE;
    @Mock private ObjectIdentifier objectIdentifier;
    @Mock private List<ObjectManage> objectsManage;

    private ServiceObject serviceObject;

    @BeforeEach
    void setUp() {
        serviceObject = new ServiceObject();
        serviceObject.setName(name);
        serviceObject.setObjectIdentifier(objectIdentifier);
        serviceObject.setHandlerType(handlerType);
        serviceObject.setObjectsManage(objectsManage);
    }

    @Test
    void testGetters() {
        assertEquals(name, serviceObject.getName());
        assertEquals(objectIdentifier, serviceObject.getObjectIdentifier());
        assertEquals(handlerType, serviceObject.getHandlerType());
        assertEquals(objectsManage, serviceObject.getObjectsManage());
    }

    @Test
    public void testEqualsAndHashCode() {
        ServiceObject obj = new ServiceObject();
        assertNotEquals(serviceObject, obj);
        assertNotEquals(serviceObject.hashCode(), obj.hashCode());

        ServiceObject serviceObject1 = new ServiceObject();
        assertNotEquals(serviceObject, serviceObject1);
        assertNotEquals(serviceObject.hashCode(), serviceObject1.hashCode());

        BeanUtils.copyProperties(serviceObject, serviceObject1);
        assertEquals(serviceObject, serviceObject1);
        assertEquals(serviceObject.hashCode(), serviceObject1.hashCode());
    }

    @Test
    void testToString() {
        String expectedString =
                "ServiceObject("
                        + "name="
                        + name
                        + ", objectIdentifier="
                        + objectIdentifier
                        + ", handlerType="
                        + handlerType
                        + ", objectsManage="
                        + objectsManage
                        + ")";
        assertEquals(expectedString, serviceObject.toString());
    }
}
