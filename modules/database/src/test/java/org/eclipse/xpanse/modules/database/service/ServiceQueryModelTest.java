/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.UUID;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

/** Test of ServiceQueryModel. */
class ServiceQueryModelTest {

    private final Csp csp = Csp.HUAWEI_CLOUD;
    private final Category category = Category.COMPUTE;
    private final String serviceName = "kafka";
    private final String serviceVersion = "1.0.0";
    private final ServiceDeploymentState serviceState = ServiceDeploymentState.DEPLOY_SUCCESS;
    private final String userId = "defaultUserId";
    private final String namespace = "defaultNamespace";
    private final UUID serviceTemplateId = UUID.randomUUID();
    private ServiceQueryModel serviceQueryTest;

    @BeforeEach
    void setUp() {
        serviceQueryTest = new ServiceQueryModel();
        serviceQueryTest.setCsp(csp);
        serviceQueryTest.setCategory(category);
        serviceQueryTest.setServiceName(serviceName);
        serviceQueryTest.setServiceVersion(serviceVersion);
        serviceQueryTest.setServiceState(serviceState);
        serviceQueryTest.setUserId(userId);
        serviceQueryTest.setNamespace(namespace);
        serviceQueryTest.setServiceTemplateId(serviceTemplateId);
    }

    @Test
    void testGetters() {
        assertEquals(csp, serviceQueryTest.getCsp());
        assertEquals(category, serviceQueryTest.getCategory());
        assertEquals(serviceName, serviceQueryTest.getServiceName());
        assertEquals(serviceVersion, serviceQueryTest.getServiceVersion());
        assertEquals(userId, serviceQueryTest.getUserId());
        assertEquals(serviceState, serviceQueryTest.getServiceState());
        assertEquals(namespace, serviceQueryTest.getNamespace());
        assertEquals(serviceTemplateId, serviceQueryTest.getServiceTemplateId());
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertNotEquals(serviceQueryTest, obj);
        assertNotEquals(serviceQueryTest.hashCode(), obj.hashCode());

        ServiceQueryModel test = new ServiceQueryModel();
        assertNotEquals(serviceQueryTest, test);
        assertNotEquals(serviceQueryTest.hashCode(), test.hashCode());

        BeanUtils.copyProperties(serviceQueryTest, test);
        assertEquals(serviceQueryTest, test);
        assertEquals(serviceQueryTest.hashCode(), test.hashCode());
    }

    @Test
    void testToString() {
        String expectedString =
                "ServiceQueryModel(csp="
                        + csp
                        + ", category="
                        + category
                        + ", serviceName="
                        + serviceName
                        + ", serviceVersion="
                        + serviceVersion
                        + ", serviceState="
                        + serviceState
                        + ", userId="
                        + userId
                        + ", namespace="
                        + namespace
                        + ", serviceTemplateId="
                        + serviceTemplateId
                        + ")";
        assertEquals(expectedString, serviceQueryTest.toString());
    }
}
