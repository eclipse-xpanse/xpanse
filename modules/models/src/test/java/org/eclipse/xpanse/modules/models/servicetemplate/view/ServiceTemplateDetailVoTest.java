/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.billing.Billing;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.FlavorsWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.InputVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.OutputVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceAction;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeManage;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceObject;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceProviderContactDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

/** Test of ServiceTemplateDetailVo. */
class ServiceTemplateDetailVoTest {

    private final UUID uuid = UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5");
    private final Category category = Category.COMPUTE;
    private final String name = "kafka";
    private final String version = "1.0.0";
    private final String eula = "eula";
    private final Csp csp = Csp.HUAWEI_CLOUD;
    private final String description = "description";
    private final String serviceVendor = "serviceVendor";
    private final String icon = "icon";
    private final Deployment deployment = new Deployment();
    private final OffsetDateTime createdTime = OffsetDateTime.now();
    private final OffsetDateTime lastModifiedTime = OffsetDateTime.now();
    private final ServiceHostingType serviceHostingType = ServiceHostingType.SELF;
    private final ServiceTemplateRegistrationState serviceTemplateRegistrationState =
            ServiceTemplateRegistrationState.APPROVED;
    private final Boolean isAvailableInCatalog = true;
    private final Boolean isReviewInProgress = false;
    @Mock private ServiceChangeManage serviceConfigurationManage;
    @Mock private ServiceProviderContactDetails serviceProviderContactDetails;
    @Mock private List<Region> regions;
    @Mock private List<InputVariable> inputVariables;
    @Mock private List<OutputVariable> outputVariables;
    @Mock private FlavorsWithPrice flavors;
    @Mock private Billing billing;
    @Mock private List<ServiceAction> serviceActions;
    @Mock private List<ServiceObject> serviceObjects;

    private ServiceTemplateDetailVo test;

    @BeforeEach
    void setUp() {
        test = new ServiceTemplateDetailVo();
        test.setServiceTemplateId(uuid);
        test.setCategory(category);
        test.setName(name);
        test.setVersion(version);
        test.setCsp(csp);
        test.setRegions(regions);
        test.setDescription(description);
        test.setServiceVendor(serviceVendor);
        test.setIcon(icon);
        test.setDeployment(deployment);
        test.setInputVariables(inputVariables);
        test.setOutputVariables(outputVariables);
        test.setFlavors(flavors);
        test.setBilling(billing);
        test.setCreatedTime(createdTime);
        test.setLastModifiedTime(lastModifiedTime);
        test.setServiceTemplateRegistrationState(serviceTemplateRegistrationState);
        test.setIsAvailableInCatalog(isAvailableInCatalog);
        test.setIsReviewInProgress(isReviewInProgress);
        test.setServiceHostingType(serviceHostingType);
        test.setServiceProviderContactDetails(serviceProviderContactDetails);
        test.setEula(eula);
        test.setServiceConfigurationManage(serviceConfigurationManage);
        test.setServiceActions(serviceActions);
        test.setServiceObjects(serviceObjects);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(uuid, test.getServiceTemplateId());
        assertEquals(category, test.getCategory());
        assertEquals(name, test.getName());
        assertEquals(version, test.getVersion());
        assertEquals(csp, test.getCsp());
        assertEquals(regions, test.getRegions());
        assertEquals(description, test.getDescription());
        assertEquals(serviceVendor, test.getServiceVendor());
        assertEquals(icon, test.getIcon());
        assertEquals(inputVariables, test.getInputVariables());
        assertEquals(outputVariables, test.getOutputVariables());
        assertEquals(flavors, test.getFlavors());
        assertEquals(billing, test.getBilling());
        assertEquals(createdTime, test.getCreatedTime());
        assertEquals(lastModifiedTime, test.getLastModifiedTime());
        assertEquals(serviceTemplateRegistrationState, test.getServiceTemplateRegistrationState());
        assertEquals(isAvailableInCatalog, test.getIsAvailableInCatalog());
        assertEquals(isReviewInProgress, test.getIsReviewInProgress());
        assertEquals(serviceHostingType, test.getServiceHostingType());
        assertEquals(serviceProviderContactDetails, test.getServiceProviderContactDetails());
        assertEquals(eula, test.getEula());
        assertEquals(serviceConfigurationManage, test.getServiceConfigurationManage());
        assertEquals(serviceActions, test.getServiceActions());
    }

    @Test
    public void testEqualsAndHashCode() {
        Object obj = new Object();
        assertThat(test).isNotEqualTo(obj);
        assertThat(test.hashCode()).isNotEqualTo(obj.hashCode());
        ServiceTemplateDetailVo test1 = new ServiceTemplateDetailVo();
        assertThat(test).isNotEqualTo(test1);
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());
        ServiceTemplateDetailVo test2 = new ServiceTemplateDetailVo();
        BeanUtils.copyProperties(test, test2);
        assertThat(test).isEqualTo(test2);
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString =
                "ServiceTemplateDetailVo(serviceTemplateId="
                        + uuid
                        + ", name="
                        + name
                        + ", version="
                        + version
                        + ", csp="
                        + csp
                        + ", category="
                        + category
                        + ", serviceVendor="
                        + serviceVendor
                        + ", regions="
                        + regions
                        + ", description="
                        + description
                        + ", icon="
                        + icon
                        + ", deployment="
                        + deployment
                        + ", inputVariables="
                        + inputVariables
                        + ", outputVariables="
                        + outputVariables
                        + ", flavors="
                        + flavors
                        + ", billing="
                        + billing
                        + ", serviceHostingType="
                        + serviceHostingType
                        + ", createdTime="
                        + createdTime
                        + ", lastModifiedTime="
                        + lastModifiedTime
                        + ", serviceTemplateRegistrationState="
                        + serviceTemplateRegistrationState
                        + ", isReviewInProgress="
                        + isReviewInProgress
                        + ", isAvailableInCatalog="
                        + isAvailableInCatalog
                        + ", serviceProviderContactDetails="
                        + serviceProviderContactDetails
                        + ", eula="
                        + eula
                        + ", serviceConfigurationManage="
                        + serviceConfigurationManage
                        + ", serviceActions="
                        + serviceActions
                        + ", serviceObjects="
                        + serviceObjects
                        + ")";

        assertEquals(expectedToString, test.toString());
    }
}
