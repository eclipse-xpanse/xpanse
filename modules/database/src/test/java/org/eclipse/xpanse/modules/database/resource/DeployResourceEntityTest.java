/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeployResourceEntityTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String RESOURCE_ID = UUID.randomUUID().toString();
    private static final String NAME = "name";
    private static final DeployResourceKind KIND = DeployResourceKind.VM;
    private static final DeployServiceEntity DEPLOY_SERVICE = new DeployServiceEntity();
    private static final Map<String, String> PROPERTIES = new HashMap<>();


    private DeployResourceEntity test;

    @BeforeEach
    void setUp() {
        test = new DeployResourceEntity();
        test.setId(ID);
        test.setResourceId(RESOURCE_ID);
        test.setName(NAME);
        test.setKind(KIND);
        test.setDeployService(DEPLOY_SERVICE);
        test.setProperties(PROPERTIES);
    }

    @Test
    void testToString() {
        String expectedToString =
                "DeployResourceEntity(id=" + ID + ", "
                        + "resourceId=" + RESOURCE_ID + ", "
                        + "name=" + NAME + ", "
                        + "kind=" + KIND + ", "
                        + "deployService=" + DEPLOY_SERVICE + ", "
                        + "properties=" + PROPERTIES + ")";
        assertEquals(expectedToString, test.toString());
    }


    @Test
    void testEqualsAndHashCode() {

        Object o = new Object();
        assertNotEquals(test, o);
        assertNotEquals(test.hashCode(), o.hashCode());

        DeployResourceEntity test1 = new DeployResourceEntity();
        DeployResourceEntity test2 = new DeployResourceEntity();
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test.hashCode(), test2.hashCode());
        assertEquals(test1.hashCode(), test2.hashCode());

        test1.setId(ID);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setResourceId(RESOURCE_ID);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setName(NAME);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setKind(KIND);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setDeployService(DEPLOY_SERVICE);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setProperties(PROPERTIES);
        assertEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());
    }
}
