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
import java.util.UUID;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.utils.ServiceDeployVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceProviderContactDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

class ServiceTemplateEntityTest {
    private final UUID id = UUID.randomUUID();
    private final ServiceTemplateRegistrationState serviceTemplateRegistrationState =
            ServiceTemplateRegistrationState.APPROVED;
    private final String namespace = "namespace";
    private final String reviewComment = "reviewComment";
    private Category category;
    private Csp csp;
    private String name;
    private String version;
    private ServiceHostingType serviceHostingType;
    private Ocl ocl;
    private JsonObjectSchema jsonObjectSchema;
    private ServiceProviderContactDetails serviceProviderContactDetails;
    private ServiceTemplateEntity testEntity;

    @BeforeEach
    void setUp() throws Exception {
        OclLoader oclLoader = new OclLoader();
        ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ServiceDeployVariablesJsonSchemaGenerator serviceDeployVariablesJsonSchemaGenerator =
                new ServiceDeployVariablesJsonSchemaGenerator();
        jsonObjectSchema = serviceDeployVariablesJsonSchemaGenerator.buildDeployVariableJsonSchema(
                ocl.getDeployment().getVariables());
        category = ocl.getCategory();
        csp = ocl.getCloudServiceProvider().getName();
        name = ocl.getName();
        version = ocl.getVersion();
        serviceProviderContactDetails = ocl.getServiceProviderContactDetails();
        serviceHostingType = ocl.getServiceHostingType();

        testEntity = new ServiceTemplateEntity();
        testEntity.setId(id);
        testEntity.setName(name);
        testEntity.setVersion(version);
        testEntity.setCsp(csp);
        testEntity.setCategory(category);
        testEntity.setNamespace(namespace);
        testEntity.setOcl(ocl);
        testEntity.setServiceHostingType(serviceHostingType);
        testEntity.setServiceTemplateRegistrationState(serviceTemplateRegistrationState);
        testEntity.setIsUpdatePending(false);
        testEntity.setAvailableInCatalog(true);
        testEntity.setReviewComment(reviewComment);
        testEntity.setJsonObjectSchema(jsonObjectSchema);
        testEntity.setServiceProviderContactDetails(serviceProviderContactDetails);
    }

    @Test
    void testGetters() {
        assertEquals(csp, testEntity.getCsp());
        assertEquals(category, testEntity.getCategory());
        assertEquals(name, testEntity.getName());
        assertEquals(version, testEntity.getVersion());
        assertEquals(namespace, testEntity.getNamespace());
        assertEquals(serviceHostingType, testEntity.getServiceHostingType());
        assertEquals(serviceTemplateRegistrationState,
                testEntity.getServiceTemplateRegistrationState());
        assertEquals(true, testEntity.getAvailableInCatalog());
        assertEquals(false, testEntity.getIsUpdatePending());
        assertEquals(jsonObjectSchema, testEntity.getJsonObjectSchema());
        assertEquals(ocl, testEntity.getOcl());
        assertEquals(id, testEntity.getId());
        assertEquals(serviceProviderContactDetails, testEntity.getServiceProviderContactDetails());
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
        String expectedToString = "ServiceTemplateEntity(id=" + id
                + ", name=" + name
                + ", version=" + version
                + ", csp=" + csp
                + ", category=" + category
                + ", namespace=" + namespace
                + ", serviceHostingType=" + serviceHostingType
                + ", ocl=" + ocl
                + ", serviceTemplateRegistrationState=" + serviceTemplateRegistrationState
                + ", isUpdatePending=false"
                + ", availableInCatalog=true"
                + ", reviewComment=" + reviewComment
                + ", serviceProviderContactDetails=" + serviceProviderContactDetails
                + ", jsonObjectSchema=" + jsonObjectSchema
                + ")";
        assertEquals(expectedToString, testEntity.toString());
    }
}
