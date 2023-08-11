/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of ServiceQueryModel.
 */
class ServiceQueryModelTest {

    private static final Csp csp = Csp.HUAWEI;
    private static final Category category = Category.COMPUTE;
    private static final String serviceName = "kafka";
    private static final String serviceVersion = "v1.0.0";
    private static final ServiceDeploymentState serviceState =
            ServiceDeploymentState.DEPLOY_SUCCESS;
    private static final String userId = "defaultUserId";
    private static ServiceQueryModel serviceQueryTest;

    @BeforeEach
    void setUp() {
        serviceQueryTest = new ServiceQueryModel();
        serviceQueryTest.setCsp(csp);
        serviceQueryTest.setCategory(category);
        serviceQueryTest.setServiceName(serviceName);
        serviceQueryTest.setServiceVersion(serviceVersion);
        serviceQueryTest.setServiceState(serviceState);
        serviceQueryTest.setMyUserId(userId);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(csp, serviceQueryTest.getCsp());
        assertEquals(category, serviceQueryTest.getCategory());
        assertEquals(serviceName, serviceQueryTest.getServiceName());
        assertEquals(serviceVersion, serviceQueryTest.getServiceVersion());
        assertEquals(userId, serviceQueryTest.getMyUserId());
        assertEquals(serviceState, serviceQueryTest.getServiceState());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(serviceQueryTest, serviceQueryTest);
        assertEquals(serviceQueryTest.hashCode(), serviceQueryTest.hashCode());

        Object obj = new Object();
        assertNotEquals(serviceQueryTest, obj);
        assertNotEquals(serviceQueryTest, null);
        assertNotEquals(serviceQueryTest.hashCode(), obj.hashCode());

        ServiceQueryModel serviceQueryTest1 = new ServiceQueryModel();
        ServiceQueryModel serviceQueryTest2 = new ServiceQueryModel();
        assertNotEquals(serviceQueryTest, serviceQueryTest1);
        assertNotEquals(serviceQueryTest, serviceQueryTest2);
        assertEquals(serviceQueryTest1, serviceQueryTest2);
        assertNotEquals(serviceQueryTest.hashCode(), serviceQueryTest1.hashCode());
        assertNotEquals(serviceQueryTest.hashCode(), serviceQueryTest2.hashCode());
        assertEquals(serviceQueryTest1.hashCode(), serviceQueryTest2.hashCode());

        serviceQueryTest1.setCsp(csp);
        assertNotEquals(serviceQueryTest, serviceQueryTest1);
        assertNotEquals(serviceQueryTest1, serviceQueryTest2);
        assertNotEquals(serviceQueryTest.hashCode(), serviceQueryTest1.hashCode());
        assertNotEquals(serviceQueryTest1.hashCode(), serviceQueryTest2.hashCode());

        serviceQueryTest1.setCategory(category);
        assertNotEquals(serviceQueryTest, serviceQueryTest1);
        assertNotEquals(serviceQueryTest1, serviceQueryTest2);
        assertNotEquals(serviceQueryTest.hashCode(), serviceQueryTest1.hashCode());
        assertNotEquals(serviceQueryTest1.hashCode(), serviceQueryTest2.hashCode());

        serviceQueryTest1.setServiceName(serviceName);
        assertNotEquals(serviceQueryTest, serviceQueryTest1);
        assertNotEquals(serviceQueryTest1, serviceQueryTest2);
        assertNotEquals(serviceQueryTest.hashCode(), serviceQueryTest1.hashCode());
        assertNotEquals(serviceQueryTest1.hashCode(), serviceQueryTest2.hashCode());

        serviceQueryTest1.setServiceVersion(serviceVersion);
        assertNotEquals(serviceQueryTest, serviceQueryTest1);
        assertNotEquals(serviceQueryTest1, serviceQueryTest2);
        assertNotEquals(serviceQueryTest.hashCode(), serviceQueryTest1.hashCode());
        assertNotEquals(serviceQueryTest1.hashCode(), serviceQueryTest2.hashCode());

        serviceQueryTest1.setServiceState(serviceState);
        assertNotEquals(serviceQueryTest, serviceQueryTest1);
        assertNotEquals(serviceQueryTest1, serviceQueryTest2);
        assertNotEquals(serviceQueryTest.hashCode(), serviceQueryTest1.hashCode());
        assertNotEquals(serviceQueryTest1.hashCode(), serviceQueryTest2.hashCode());

        serviceQueryTest1.setMyUserId(userId);
        assertEquals(serviceQueryTest, serviceQueryTest1);
        assertNotEquals(serviceQueryTest1, serviceQueryTest2);
        assertEquals(serviceQueryTest.hashCode(), serviceQueryTest1.hashCode());
        assertNotEquals(serviceQueryTest1.hashCode(), serviceQueryTest2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "ServiceQueryModel(csp=HUAWEI, category=COMPUTE, serviceName=kafka,"
                + " serviceVersion=v1.0.0, serviceState=DEPLOY_SUCCESS, myUserId=defaultUserId)";
        assertEquals(expectedString, serviceQueryTest.toString());
    }

}
