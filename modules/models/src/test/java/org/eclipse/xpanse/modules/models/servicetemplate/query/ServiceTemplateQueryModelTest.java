/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of RegisteredServiceQuery.
 */
class ServiceTemplateQueryModelTest {

    private static final Csp csp = Csp.HUAWEI;
    private static final Category category = Category.COMPUTE;
    private static final String serviceName = "kafka";
    private static final String serviceVersion = "v1.0.0";
    private static final String namespace = "huawei";
    private static ServiceTemplateQueryModel serviceTemplateQueryModel;

    @BeforeEach
    void setUp() {
        serviceTemplateQueryModel = new ServiceTemplateQueryModel();
        serviceTemplateQueryModel.setCsp(csp);
        serviceTemplateQueryModel.setCategory(category);
        serviceTemplateQueryModel.setServiceName(serviceName);
        serviceTemplateQueryModel.setServiceVersion(serviceVersion);
        serviceTemplateQueryModel.setNamespace(namespace);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(csp, serviceTemplateQueryModel.getCsp());
        assertEquals(category, serviceTemplateQueryModel.getCategory());
        assertEquals(serviceName, serviceTemplateQueryModel.getServiceName());
        assertEquals(serviceVersion, serviceTemplateQueryModel.getServiceVersion());
        assertEquals(namespace, serviceTemplateQueryModel.getNamespace());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(serviceTemplateQueryModel, serviceTemplateQueryModel);
        assertEquals(serviceTemplateQueryModel.hashCode(), serviceTemplateQueryModel.hashCode());

        Object obj = new Object();
        assertNotEquals(serviceTemplateQueryModel, obj);
        assertNotEquals(serviceTemplateQueryModel, null);
        assertNotEquals(serviceTemplateQueryModel.hashCode(), obj.hashCode());

        ServiceTemplateQueryModel serviceTemplateQueryModel1 = new ServiceTemplateQueryModel();
        ServiceTemplateQueryModel serviceTemplateQueryModel2 = new ServiceTemplateQueryModel();
        assertNotEquals(serviceTemplateQueryModel, serviceTemplateQueryModel1);
        assertNotEquals(serviceTemplateQueryModel, serviceTemplateQueryModel2);
        assertEquals(serviceTemplateQueryModel1, serviceTemplateQueryModel2);
        assertNotEquals(serviceTemplateQueryModel.hashCode(),
                serviceTemplateQueryModel1.hashCode());
        assertNotEquals(serviceTemplateQueryModel.hashCode(),
                serviceTemplateQueryModel2.hashCode());
        assertEquals(serviceTemplateQueryModel1.hashCode(), serviceTemplateQueryModel2.hashCode());

        serviceTemplateQueryModel1.setCsp(csp);
        assertNotEquals(serviceTemplateQueryModel, serviceTemplateQueryModel1);
        assertNotEquals(serviceTemplateQueryModel1, serviceTemplateQueryModel2);
        assertNotEquals(serviceTemplateQueryModel.hashCode(),
                serviceTemplateQueryModel1.hashCode());
        assertNotEquals(serviceTemplateQueryModel1.hashCode(),
                serviceTemplateQueryModel2.hashCode());

        serviceTemplateQueryModel1.setCategory(category);
        assertNotEquals(serviceTemplateQueryModel, serviceTemplateQueryModel1);
        assertNotEquals(serviceTemplateQueryModel1, serviceTemplateQueryModel2);
        assertNotEquals(serviceTemplateQueryModel.hashCode(),
                serviceTemplateQueryModel1.hashCode());
        assertNotEquals(serviceTemplateQueryModel1.hashCode(),
                serviceTemplateQueryModel2.hashCode());

        serviceTemplateQueryModel1.setServiceName(serviceName);
        assertNotEquals(serviceTemplateQueryModel, serviceTemplateQueryModel1);
        assertNotEquals(serviceTemplateQueryModel1, serviceTemplateQueryModel2);
        assertNotEquals(serviceTemplateQueryModel.hashCode(),
                serviceTemplateQueryModel1.hashCode());
        assertNotEquals(serviceTemplateQueryModel1.hashCode(),
                serviceTemplateQueryModel2.hashCode());

        serviceTemplateQueryModel1.setServiceVersion(serviceVersion);
        assertNotEquals(serviceTemplateQueryModel, serviceTemplateQueryModel1);
        assertNotEquals(serviceTemplateQueryModel1, serviceTemplateQueryModel2);
        assertNotEquals(serviceTemplateQueryModel.hashCode(),
                serviceTemplateQueryModel1.hashCode());
        assertNotEquals(serviceTemplateQueryModel1.hashCode(),
                serviceTemplateQueryModel2.hashCode());

        serviceTemplateQueryModel1.setNamespace(namespace);
        assertEquals(serviceTemplateQueryModel, serviceTemplateQueryModel1);
        assertNotEquals(serviceTemplateQueryModel1, serviceTemplateQueryModel2);
        assertEquals(serviceTemplateQueryModel.hashCode(), serviceTemplateQueryModel1.hashCode());
        assertNotEquals(serviceTemplateQueryModel1.hashCode(),
                serviceTemplateQueryModel2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "ServiceTemplateQueryModel(" +
                "csp=" + csp +
                ", category=" + category + "" +
                ", serviceName=" + serviceName + "" +
                ", serviceVersion=" + serviceVersion + "" +
                ", namespace=" + namespace + "" +
                ")";
        assertEquals(expectedString, serviceTemplateQueryModel.toString());
    }

}
