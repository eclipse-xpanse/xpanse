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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

/**
 * Test of ServiceTemplateQueryModel.
 */
class ServiceTemplateQueryModelTest {

    private static final Csp csp = Csp.HUAWEI;
    private static final Category category = Category.COMPUTE;
    private static final String serviceName = "kafka";
    private static final String serviceVersion = "v1.0.0";
    private static final String namespace = "huawei";
    private static final ServiceHostingType serviceHostingType = ServiceHostingType.SELF;
    private static final boolean checkNamespace = true;
    private static ServiceTemplateQueryModel serviceTemplateQueryModel;

    @BeforeEach
    void setUp() {
        serviceTemplateQueryModel = new ServiceTemplateQueryModel();
        serviceTemplateQueryModel.setCsp(csp);
        serviceTemplateQueryModel.setCategory(category);
        serviceTemplateQueryModel.setServiceName(serviceName);
        serviceTemplateQueryModel.setServiceVersion(serviceVersion);
        serviceTemplateQueryModel.setNamespace(namespace);
        serviceTemplateQueryModel.setServiceHostingType(serviceHostingType);
        serviceTemplateQueryModel.setCheckNamespace(checkNamespace);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(csp, serviceTemplateQueryModel.getCsp());
        assertEquals(category, serviceTemplateQueryModel.getCategory());
        assertEquals(serviceName, serviceTemplateQueryModel.getServiceName());
        assertEquals(serviceVersion, serviceTemplateQueryModel.getServiceVersion());
        assertEquals(namespace, serviceTemplateQueryModel.getNamespace());
        assertEquals(serviceHostingType, serviceTemplateQueryModel.getServiceHostingType());
        assertEquals(checkNamespace, serviceTemplateQueryModel.isCheckNamespace());
    }

    @Test
    void testEquals() {
        ServiceTemplateQueryModel test1 = new ServiceTemplateQueryModel();
        assertNotEquals(serviceTemplateQueryModel, test1);

        BeanUtils.copyProperties(serviceTemplateQueryModel, test1);
        assertEquals(serviceTemplateQueryModel, test1);
    }

    @Test
    void testCanEqual() {
        assertFalse(serviceTemplateQueryModel.canEqual("other"));
    }

    @Test
    void testHashCode() {
        ServiceTemplateQueryModel test1 = new ServiceTemplateQueryModel();
        assertNotEquals(serviceTemplateQueryModel.hashCode(), test1.hashCode());

        BeanUtils.copyProperties(serviceTemplateQueryModel, test1);
        assertEquals(serviceTemplateQueryModel.hashCode(), test1.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "ServiceTemplateQueryModel(" +
                "csp=" + csp +
                ", category=" + category +
                ", serviceName=" + serviceName +
                ", serviceVersion=" + serviceVersion +
                ", namespace=" + namespace +
                ", serviceHostingType=" + serviceHostingType +
                ", checkNamespace=" + checkNamespace +
                ")";
        assertEquals(expectedString, serviceTemplateQueryModel.toString());
    }

}
