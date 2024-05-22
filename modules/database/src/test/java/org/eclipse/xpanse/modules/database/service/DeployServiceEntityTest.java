/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.enums.ServiceState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

class DeployServiceEntityTest {

    private final OffsetDateTime LAST_STARTED_AT = OffsetDateTime.now();
    private final OffsetDateTime LAST_STOPPED_AT = OffsetDateTime.now();
    private final ServiceLockConfig LOCK_CONFIG = new ServiceLockConfig();
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
        test.setLockConfig(LOCK_CONFIG);
    }

    @Test
    void testGetters() {
        assertEquals(ID, test.getId());
        assertEquals(USER_ID, test.getUserId());
        assertEquals(CATEGORY, test.getCategory());
        assertEquals(NAME, test.getName());
        assertEquals(CUSTOMER_SERVICE_NAME, test.getCustomerServiceName());
        assertEquals(VERSION, test.getVersion());
        assertEquals(NAMESPACE, test.getNamespace());
        assertEquals(CSP, test.getCsp());
        assertEquals(FLAVOR, test.getFlavor());
        assertEquals(SERVICE_STATE, test.getServiceDeploymentState());
        assertEquals(SERVICE_RUN_STATE, test.getServiceState());
        assertEquals(SERVICE_TEMPLATE_ID, test.getServiceTemplateId());
        assertEquals(CREATE_REQUEST, test.getDeployRequest());
        assertEquals(DEPLOY_RESOURCE_LIST, test.getDeployResourceList());
        assertEquals(PROPERTIES, test.getProperties());
        assertEquals(PRIVATE_PROPERTIES, test.getPrivateProperties());
        assertEquals(RESULT_MESSAGE, test.getResultMessage());
        assertEquals(LAST_STARTED_AT, test.getLastStartedAt());
        assertEquals(LAST_STOPPED_AT, test.getLastStoppedAt());
        assertEquals(LOCK_CONFIG, test.getLockConfig());
    }

    @Test
    void testToString() {
        String expectedToString =
                "DeployServiceEntity(id=" + ID + ", userId=" + USER_ID + ", category=" + CATEGORY
                        + ", name=" + NAME + ", customerServiceName=" + CUSTOMER_SERVICE_NAME
                        + ", version=" + VERSION + ", namespace=" + NAMESPACE + ", csp=" + CSP
                        + ", flavor=" + FLAVOR + ", serviceDeploymentState=" + SERVICE_STATE
                        + ", resultMessage=" + RESULT_MESSAGE + ", serviceState="
                        + SERVICE_RUN_STATE + ", serviceTemplateId=" + SERVICE_TEMPLATE_ID
                        + ", deployRequest=" + CREATE_REQUEST + ", properties=" + PROPERTIES
                        + ", privateProperties=" + PRIVATE_PROPERTIES + ", lastStartedAt="
                        + LAST_STARTED_AT + ", lastStoppedAt=" + LAST_STOPPED_AT + ", lockConfig="
                        + LOCK_CONFIG + ")";
        assertEquals(expectedToString, test.toString());
    }


    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertFalse(test.canEqual(o));
        assertNotEquals(test, o);
        assertNotEquals(test.hashCode(), o.hashCode());

        DeployServiceEntity test1 = new DeployServiceEntity();
        assertTrue(test.canEqual(test1));
        assertNotEquals(test, test1);
        assertNotEquals(test.hashCode(), test1.hashCode());

        BeanUtils.copyProperties(test, test1);
        assertTrue(test.canEqual(test1));
        assertEquals(test, test1);
        assertEquals(test.hashCode(), test1.hashCode());
    }
}
