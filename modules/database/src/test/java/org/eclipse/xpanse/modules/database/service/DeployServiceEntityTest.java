/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeployServiceEntityTest {

    private final UUID ID = UUID.fromString("eef27308-92d6-4c7a-866b-a58966b94f2d");
    private final String USER_ID = "defaultUserId";
    private final Category CATEGORY = Category.MIDDLEWARE;
    private final String NAME = "kafka";
    private final String CUSTOMER_SERVICE_NAME = "kafka-cluster";
    private final String VERSION = "2.0";
    private final String NAMESPACE = "defaultUserId";
    private final Csp CSP = Csp.HUAWEI;
    private final String FLAVOR = "1-zookeeper-with-3-worker-nodes-normal";
    private final ServiceDeploymentState SERVICE_STATE = ServiceDeploymentState.DEPLOYING;
    private final ServiceState SERVICE_RUN_STATE = ServiceState.NOT_RUNNING;
    private final UUID SERVICE_TEMPLATE_ID =
            UUID.fromString("eef27308-92d6-4c7a-866b-a58966b94f3d");
    private final DeployRequest CREATE_REQUEST = new DeployRequest();
    private final List<DeployResourceEntity> DEPLOY_RESOURCE_LIST = new ArrayList<>();
    private final Map<String, String> PROPERTIES = new HashMap<>();
    private final Map<String, String> PRIVATE_PROPERTIES = new HashMap<>();
    private final String RESULT_MESSAGE = "RESULT_MESSAGE";
    private static final OffsetDateTime LAST_STARTED_AT = OffsetDateTime.now();
    private static final OffsetDateTime LAST_STOPPED_AT = OffsetDateTime.now();


    private DeployServiceEntity test;

    @BeforeEach
    void setUp() {
        test = new DeployServiceEntity();
        test.setId(ID);
        test.setUserId(USER_ID);
        test.setCategory(CATEGORY);
        test.setName(NAME);
        test.setCustomerServiceName(CUSTOMER_SERVICE_NAME);
        test.setVersion(VERSION);
        test.setNamespace(NAMESPACE);
        test.setCsp(CSP);
        test.setFlavor(FLAVOR);
        test.setServiceDeploymentState(SERVICE_STATE);
        test.setServiceState(SERVICE_RUN_STATE);
        test.setServiceTemplateId(SERVICE_TEMPLATE_ID);
        test.setDeployRequest(CREATE_REQUEST);
        test.setDeployResourceList(DEPLOY_RESOURCE_LIST);
        test.setProperties(PROPERTIES);
        test.setPrivateProperties(PRIVATE_PROPERTIES);
        test.setResultMessage(RESULT_MESSAGE);
        test.setLastStartedAt(LAST_STARTED_AT);
        test.setLastStoppedAt(LAST_STOPPED_AT);
    }

    @Test
    void testToString() {
        String expectedToString =
                "DeployServiceEntity(id=" + ID + ", "
                        + "userId=" + USER_ID + ", "
                        + "category=" + CATEGORY + ", "
                        + "name=" + NAME + ", "
                        + "customerServiceName=" + CUSTOMER_SERVICE_NAME + ", "
                        + "version=" + VERSION + ", "
                        + "namespace=" + NAMESPACE + ", "
                        + "csp=" + CSP + ", "
                        + "flavor=" + FLAVOR + ", "
                        + "serviceDeploymentState=" + SERVICE_STATE + ", "
                        + "serviceState=" + SERVICE_RUN_STATE + ", "
                        + "serviceTemplateId=" + SERVICE_TEMPLATE_ID + ", "
                        + "deployRequest=" + CREATE_REQUEST + ", "
                        //+ "deployResourceList=" + DEPLOY_RESOURCE_LIST+ ", "
                        + "properties=" + PROPERTIES + ", "
                        + "privateProperties=" + PRIVATE_PROPERTIES + ", "
                        + "resultMessage=" + RESULT_MESSAGE + ", "
                        + "lastStartedAt=" + LAST_STARTED_AT + ", "
                        + "lastStoppedAt=" + LAST_STOPPED_AT + ")";
        Assertions.assertEquals(expectedToString, test.toString());
    }


    @Test
    void testEqualsAndHashCode() {
        Assertions.assertEquals(test, test);
        Assertions.assertEquals(test.hashCode(), test.hashCode());

        Object o = new Object();
        Assertions.assertNotEquals(test, o);
        Assertions.assertNotEquals(test.hashCode(), o.hashCode());

        DeployServiceEntity test1 = new DeployServiceEntity();
        DeployServiceEntity test2 = new DeployServiceEntity();
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test.hashCode(), test2.hashCode());
        Assertions.assertEquals(test1.hashCode(), test2.hashCode());

        test1.setId(ID);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setUserId(USER_ID);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setCategory(CATEGORY);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setName(NAME);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setCustomerServiceName(CUSTOMER_SERVICE_NAME);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setVersion(VERSION);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setNamespace(NAMESPACE);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());


        test1.setCsp(CSP);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setFlavor(FLAVOR);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setServiceDeploymentState(SERVICE_STATE);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setServiceTemplateId(SERVICE_TEMPLATE_ID);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setDeployRequest(CREATE_REQUEST);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setDeployResourceList(DEPLOY_RESOURCE_LIST);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setProperties(PROPERTIES);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setPrivateProperties(PRIVATE_PROPERTIES);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setResultMessage(RESULT_MESSAGE);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setLastStartedAt(LAST_STARTED_AT);
        Assertions.assertNotEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertNotEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());

        test1.setLastStoppedAt(LAST_STOPPED_AT);
        Assertions.assertEquals(test, test1);
        Assertions.assertNotEquals(test, test2);
        Assertions.assertNotEquals(test1, test2);
        Assertions.assertEquals(test.hashCode(), test1.hashCode());
        Assertions.assertNotEquals(test1.hashCode(), test2.hashCode());
    }
}
