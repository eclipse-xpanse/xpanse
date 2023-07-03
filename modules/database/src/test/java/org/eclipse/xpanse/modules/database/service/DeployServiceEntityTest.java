/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.CreateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeployServiceEntityTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String USER_NAME = "user";
    private static final Category CATEGORY = Category.MIDDLEWARE;
    private static final String NAME = "kafka";
    private static final String CUSTOMER_SERVICE_NAME = "kafka-cluster";
    private static final String VERSION = "2.0";
    private static final Csp CSP = Csp.HUAWEI;
    private static final String FLAVOR = "1-zookeeper-with-3-worker-nodes-normal";
    private static final ServiceDeploymentState SERVICE_STATE = ServiceDeploymentState.DEPLOYING;
    private static final CreateRequest CREATE_REQUEST = new CreateRequest();
    private static final List<DeployResourceEntity> DEPLOY_RESOURCE_LIST = new ArrayList<>();
    private static final Map<String, String> PROPERTIES = new HashMap<>();
    private static final Map<String, String> PRIVATE_PROPERTIES = new HashMap<>();
    private static final String RESULT_MESSAGE = "RESULT_MESSAGE";


    private DeployServiceEntity test;

    @BeforeEach
    void setUp() {
        test = new DeployServiceEntity();
        test.setId(ID);
        test.setUserName(USER_NAME);
        test.setCategory(CATEGORY);
        test.setName(NAME);
        test.setCustomerServiceName(CUSTOMER_SERVICE_NAME);
        test.setVersion(VERSION);
        test.setCsp(CSP);
        test.setFlavor(FLAVOR);
        test.setServiceDeploymentState(SERVICE_STATE);
        test.setCreateRequest(CREATE_REQUEST);
        test.setDeployResourceList(DEPLOY_RESOURCE_LIST);
        test.setProperties(PROPERTIES);
        test.setPrivateProperties(PRIVATE_PROPERTIES);
        test.setResultMessage(RESULT_MESSAGE);
    }

    @Test
    void testToString() {
        String expectedToString =
                "DeployServiceEntity(id=" + ID + ", "
                        + "userName=" + USER_NAME + ", "
                        + "category=" + CATEGORY + ", "
                        + "name=" + NAME + ", "
                        + "customerServiceName=" + CUSTOMER_SERVICE_NAME + ", "
                        + "version=" + VERSION + ", "
                        + "csp=" + CSP + ", "
                        + "flavor=" + FLAVOR + ", "
                        + "serviceDeploymentState=" + SERVICE_STATE + ", "
                        + "createRequest=" + CREATE_REQUEST + ", "
                        //+ "deployResourceList=" + DEPLOY_RESOURCE_LIST+ ", "
                        + "properties=" + PROPERTIES + ", "
                        + "privateProperties=" + PRIVATE_PROPERTIES + ", "
                        + "resultMessage=" + RESULT_MESSAGE + ")";
        assertEquals(expectedToString, test.toString());
    }


    @Test
    void testEqualsAndHashCode() {
        assertEquals(test, test);
        assertEquals(test.hashCode(), test.hashCode());

        Object o = new Object();
        assertNotEquals(test, o);
        assertNotEquals(test.hashCode(), o.hashCode());

        DeployServiceEntity test1 = new DeployServiceEntity();
        DeployServiceEntity test2 = new DeployServiceEntity();
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

        test1.setUserName(USER_NAME);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setCategory(CATEGORY);
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

        test1.setCustomerServiceName(CUSTOMER_SERVICE_NAME);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setVersion(VERSION);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setCsp(CSP);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setFlavor(FLAVOR);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setServiceDeploymentState(SERVICE_STATE);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setCreateRequest(CREATE_REQUEST);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setDeployResourceList(DEPLOY_RESOURCE_LIST);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setProperties(PROPERTIES);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setPrivateProperties(PRIVATE_PROPERTIES);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setResultMessage(RESULT_MESSAGE);
        assertEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test1, test2);
        assertEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test1.hashCode(), test2.hashCode());
    }
}
