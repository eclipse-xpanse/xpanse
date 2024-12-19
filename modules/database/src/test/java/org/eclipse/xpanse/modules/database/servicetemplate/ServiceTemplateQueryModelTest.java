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
    private final Boolean checkServiceVendor = true;
    private final String serviceVendor = "HuaweiCloud";
    private final Boolean isAvailableInCatalog = true;
    private final Boolean isReviewInProgress = false;
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
                        .checkServiceVendor(checkServiceVendor)
                        .isAvailableInCatalog(isAvailableInCatalog)
                        .isReviewInProgress(isReviewInProgress)
                        .serviceVendor(serviceVendor)
                        .build();
    }

    @Test
    void testGetters() {
        assertEquals(csp, testModel.getCsp());
        assertEquals(category, testModel.getCategory());
        assertEquals(serviceName, testModel.getServiceName());
        assertEquals(serviceVersion, testModel.getServiceVersion());
        assertEquals(serviceVendor, testModel.getServiceVendor());
        assertEquals(serviceHostingType, testModel.getServiceHostingType());
        assertEquals(
                serviceTemplateRegistrationState, testModel.getServiceTemplateRegistrationState());
        assertEquals(checkServiceVendor, testModel.getCheckServiceVendor());
        assertEquals(isAvailableInCatalog, testModel.getIsAvailableInCatalog());
        assertEquals(isReviewInProgress, testModel.getIsReviewInProgress());
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
                        + ", isAvailableInCatalog="
                        + isAvailableInCatalog
                        + ", isReviewInProgress="
                        + isReviewInProgress
                        + ", checkServiceVendor="
                        + checkServiceVendor
                        + ", serviceVendor="
                        + serviceVendor
                        + ")";
        assertEquals(expectedString, testModel.toString());
    }
}
