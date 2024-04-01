/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicetemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of ServiceTemplateQueryModel.
 */
class ServiceTemplateQueryModelTest {

    private final Category category = Category.COMPUTE;
    private final Csp csp = Csp.HUAWEI;
    private final String serviceName = "kafka";
    private final String serviceVersion = "1.0.0";
    private final ServiceHostingType serviceHostingType = ServiceHostingType.SELF;
    private final ServiceRegistrationState serviceRegistrationState =
            ServiceRegistrationState.APPROVED;
    private final boolean checkNamespace = true;
    private final String namespace = "huawei";
    private ServiceTemplateQueryModel testModel;

    @BeforeEach
    void setUp() {
        testModel = new ServiceTemplateQueryModel(category, csp, serviceName, serviceVersion,
                serviceHostingType, serviceRegistrationState, checkNamespace);
        testModel.setNamespace(namespace);
    }

    @Test
    void testGetters() {
        assertEquals(csp, testModel.getCsp());
        assertEquals(category, testModel.getCategory());
        assertEquals(serviceName, testModel.getServiceName());
        assertEquals(serviceVersion, testModel.getServiceVersion());
        assertEquals(namespace, testModel.getNamespace());
        assertEquals(serviceHostingType, testModel.getServiceHostingType());
        assertEquals(ServiceRegistrationState.APPROVED, testModel.getServiceRegistrationState());
        assertEquals(checkNamespace, testModel.isCheckNamespace());
    }

    @Test
    void testEquals() {
        ServiceTemplateQueryModel test =
                new ServiceTemplateQueryModel(category, csp, serviceName, serviceVersion,
                        serviceHostingType, serviceRegistrationState, checkNamespace);
        assertNotEquals(testModel, test);

        ServiceTemplateQueryModel test1 =
                new ServiceTemplateQueryModel(category, csp, serviceName, serviceVersion,
                        serviceHostingType, serviceRegistrationState, checkNamespace);
        test1.setNamespace(namespace);
        assertEquals(testModel, test1);
    }

    @Test
    void testCanEqual() {
        assertFalse(testModel.canEqual("other"));
    }

    @Test
    void testHashCode() {
        ServiceTemplateQueryModel test =
                new ServiceTemplateQueryModel(category, csp, serviceName, serviceVersion,
                        serviceHostingType, serviceRegistrationState, checkNamespace);
        test.setNamespace("other");
        assertNotEquals(testModel.hashCode(), test.hashCode());

        ServiceTemplateQueryModel test1 =
                new ServiceTemplateQueryModel(category, csp, serviceName, serviceVersion,
                        serviceHostingType, serviceRegistrationState, checkNamespace);
        test1.setNamespace(namespace);
        assertEquals(testModel.hashCode(), test1.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "ServiceTemplateQueryModel(" +
                "category=" + category +
                ", csp=" + csp +
                ", serviceName=" + serviceName +
                ", serviceVersion=" + serviceVersion +
                ", serviceHostingType=" + serviceHostingType +
                ", serviceRegistrationState=" + serviceRegistrationState +
                ", checkNamespace=" + checkNamespace +
                ", namespace=" + namespace +
                ")";
        assertEquals(expectedString, testModel.toString());
    }

}
