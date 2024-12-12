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
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

/** Test of ServiceTemplateQueryModel. */
class ServiceTemplateQueryModelTest {

    private final Category category = Category.COMPUTE;
    private final Csp csp = Csp.HUAWEI_CLOUD;
    private final String serviceName = "kafka";
    private final String serviceVersion = "1.0.0";
    private final ServiceHostingType serviceHostingType = ServiceHostingType.SELF;
    private final ServiceTemplateRegistrationState serviceTemplateRegistrationState =
            ServiceTemplateRegistrationState.APPROVED;
    private final Boolean checkNamespace = true;
    private final String namespace = "HuaweiCloud";
    private final Boolean availableInCatalog = true;
    private final Boolean isUpdatePending = false;
    private ServiceTemplateQueryModel testModel;

    @BeforeEach
    void setUp() {
        testModel =
                ServiceTemplateQueryModel.builder()
                        .category(category)
                        .csp(csp)
                        .serviceName(serviceName)
                        .serviceVersion(serviceVersion)
                        .serviceHostingType(serviceHostingType)
                        .serviceTemplateRegistrationState(serviceTemplateRegistrationState)
                        .checkNamespace(checkNamespace)
                        .availableInCatalog(availableInCatalog)
                        .isUpdatePending(isUpdatePending)
                        .namespace(namespace)
                        .build();
    }

    @Test
    void testGetters() {
        assertEquals(csp, testModel.getCsp());
        assertEquals(category, testModel.getCategory());
        assertEquals(serviceName, testModel.getServiceName());
        assertEquals(serviceVersion, testModel.getServiceVersion());
        assertEquals(namespace, testModel.getNamespace());
        assertEquals(serviceHostingType, testModel.getServiceHostingType());
        assertEquals(
                serviceTemplateRegistrationState, testModel.getServiceTemplateRegistrationState());
        assertEquals(checkNamespace, testModel.getCheckNamespace());
        assertEquals(availableInCatalog, testModel.getAvailableInCatalog());
        assertEquals(isUpdatePending, testModel.getIsUpdatePending());
    }

    @Test
    void testEqualsAndHashCode() {
        ServiceTemplateQueryModel test = ServiceTemplateQueryModel.builder().build();
        assertNotEquals(testModel, test);
        assertNotEquals(testModel.hashCode(), test.hashCode());

        ServiceTemplateQueryModel test1 = ServiceTemplateQueryModel.builder().build();
        BeanUtils.copyProperties(testModel, test1);
        assertEquals(testModel, test1);
        assertEquals(testModel.hashCode(), test1.hashCode());
    }

    @Test
    void testCanEqual() {
        assertFalse(testModel.canEqual("other"));
    }

    @Test
    void testToString() {
        String expectedString =
                "ServiceTemplateQueryModel("
                        + "category="
                        + category
                        + ", csp="
                        + csp
                        + ", serviceName="
                        + serviceName
                        + ", serviceVersion="
                        + serviceVersion
                        + ", serviceHostingType="
                        + serviceHostingType
                        + ", serviceTemplateRegistrationState="
                        + serviceTemplateRegistrationState
                        + ", availableInCatalog="
                        + availableInCatalog
                        + ", isUpdatePending="
                        + isUpdatePending
                        + ", checkNamespace="
                        + checkNamespace
                        + ", namespace="
                        + namespace
                        + ")";
        assertEquals(expectedString, testModel.toString());
    }
}
