/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicetemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.servicepolicy.ServicePolicyEntity;
import org.eclipse.xpanse.modules.database.servicetemplaterequest.ServiceTemplateRequestHistoryEntity;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.utils.ServiceInputVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceProviderContactDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.TerraformDeployment;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class ServiceTemplateEntityTest {
    private final UUID id = UUID.randomUUID();
    private final ServiceTemplateRegistrationState serviceTemplateRegistrationState =
            ServiceTemplateRegistrationState.APPROVED;
    private final String serviceVendor = "serviceVendor";
    private final Boolean isAvailableInCatalog = false;
    private final Boolean isReviewInProgress = false;
    private Category category;
    private Csp csp;
    private String name;
    private String version;
    private String shortCode;
    private ServiceHostingType serviceHostingType;
    private Ocl ocl;
    private JsonObjectSchema jsonObjectSchema;
    private ServiceProviderContactDetails serviceProviderContactDetails;
    private ServiceTemplateEntity testEntity;
    @Mock private List<ServicePolicyEntity> mockServicePolicyList;
    @Mock private List<ServiceTemplateRequestHistoryEntity> mockServiceTemplateHistory;

    @BeforeEach
    void setUp() throws Exception {
        OclLoader oclLoader = new OclLoader();
        ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ServiceInputVariablesJsonSchemaGenerator serviceInputVariablesJsonSchemaGenerator =
                new ServiceInputVariablesJsonSchemaGenerator();
        TerraformDeployment terraformDeployment = ocl.getDeployment().getTerraformDeployment();
        jsonObjectSchema =
                serviceInputVariablesJsonSchemaGenerator.buildJsonSchemaOfInputVariables(
                        terraformDeployment.getInputVariables());
        category = ocl.getCategory();
        csp = ocl.getCloudServiceProvider().getName();
        name = ocl.getName();
        version = ocl.getVersion();
        shortCode = ocl.getShortCode();
        serviceProviderContactDetails = ocl.getServiceProviderContactDetails();
        serviceHostingType = ocl.getServiceHostingType();

        testEntity = new ServiceTemplateEntity();
        testEntity.setId(id);
        testEntity.setName(name);
        testEntity.setVersion(version);
        testEntity.setCsp(csp);
        testEntity.setCategory(category);
        testEntity.setServiceVendor(serviceVendor);
        testEntity.setOcl(ocl);
        testEntity.setServiceHostingType(serviceHostingType);
        testEntity.setServiceTemplateRegistrationState(serviceTemplateRegistrationState);
        testEntity.setIsReviewInProgress(isReviewInProgress);
        testEntity.setIsAvailableInCatalog(isAvailableInCatalog);
        testEntity.setJsonObjectSchema(jsonObjectSchema);
        testEntity.setServiceProviderContactDetails(serviceProviderContactDetails);
        testEntity.setServicePolicyList(mockServicePolicyList);
        testEntity.setServiceTemplateHistory(mockServiceTemplateHistory);
        testEntity.setShortCode(shortCode);
    }

    @Test
    void testGetters() {
        assertEquals(csp, testEntity.getCsp());
        assertEquals(category, testEntity.getCategory());
        assertEquals(name, testEntity.getName());
        assertEquals(version, testEntity.getVersion());
        assertEquals(serviceVendor, testEntity.getServiceVendor());
        assertEquals(serviceHostingType, testEntity.getServiceHostingType());
        assertEquals(
                serviceTemplateRegistrationState, testEntity.getServiceTemplateRegistrationState());
        assertEquals(isAvailableInCatalog, testEntity.getIsAvailableInCatalog());
        assertEquals(isReviewInProgress, testEntity.getIsReviewInProgress());
        assertEquals(jsonObjectSchema, testEntity.getJsonObjectSchema());
        assertEquals(ocl, testEntity.getOcl());
        assertEquals(id, testEntity.getId());
        assertEquals(isAvailableInCatalog, testEntity.getIsAvailableInCatalog());
        assertEquals(isReviewInProgress, testEntity.getIsReviewInProgress());
        assertEquals(serviceProviderContactDetails, testEntity.getServiceProviderContactDetails());
        assertEquals(mockServicePolicyList, testEntity.getServicePolicyList());
        assertEquals(mockServiceTemplateHistory, testEntity.getServiceTemplateHistory());
        assertEquals(shortCode, testEntity.getShortCode());
    }

    @Test
    void testEqualsAndHashCode() {
        ServiceTemplateEntity test = new ServiceTemplateEntity();
        assertNotEquals(testEntity, test);
        assertNotEquals(testEntity.hashCode(), test.hashCode());
        ServiceTemplateEntity test1 = new ServiceTemplateEntity();
        BeanUtils.copyProperties(testEntity, test1);
        assertEquals(testEntity, test1);
        assertEquals(testEntity.hashCode(), test1.hashCode());
    }

    @Test
    void testCanEqual() {
        assertFalse(testEntity.canEqual("other"));
    }

    @Test
    void testToString() {
        String expectedToString =
                "ServiceTemplateEntity(id="
                        + id
                        + ", name="
                        + name
                        + ", shortCode="
                        + shortCode
                        + ", version="
                        + version
                        + ", csp="
                        + csp
                        + ", category="
                        + category
                        + ", serviceVendor="
                        + serviceVendor
                        + ", serviceHostingType="
                        + serviceHostingType
                        + ", ocl="
                        + ocl
                        + ", serviceTemplateRegistrationState="
                        + serviceTemplateRegistrationState
                        + ", isReviewInProgress="
                        + isReviewInProgress
                        + ", isAvailableInCatalog="
                        + isAvailableInCatalog
                        + ", serviceProviderContactDetails="
                        + serviceProviderContactDetails
                        + ", jsonObjectSchema="
                        + jsonObjectSchema
                        + ")";
        assertEquals(expectedToString, testEntity.toString());
    }
}
