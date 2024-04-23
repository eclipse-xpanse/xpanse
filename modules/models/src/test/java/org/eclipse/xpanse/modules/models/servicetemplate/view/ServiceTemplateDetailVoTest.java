/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Billing;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.Flavors;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavor;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceProviderContactDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.BillingMode;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

/**
 * Test of ServiceTemplateDetailVo.
 */
class ServiceTemplateDetailVoTest {

    private final UUID uuid = UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5");
    private final Category category = Category.COMPUTE;
    private final String name = "kafka";
    private final String version = "1.0.0";
    private final Csp csp = Csp.AWS;
    private final String description = "description";
    private final String namespace = "namespace";
    private final String icon = "icon";
    private final Deployment DEPLOYMENT = new Deployment();
    private final OffsetDateTime createTime = OffsetDateTime.now();
    private final OffsetDateTime lastModifiedTime = OffsetDateTime.now();
    private final ServiceRegistrationState serviceRegistrationState =
            ServiceRegistrationState.APPROVED;
    private final String reviewComment = "reviewComment";
    @Mock
    private ServiceProviderContactDetails serviceProviderContactDetails;
    private List<Region> regions;
    private List<DeployVariable> variables;
    private Flavors flavors;
    private Billing billing;
    private ServiceTemplateDetailVo serviceTemplateDetailVo;

    @BeforeEach
    void setUp() {
        Region region = new Region();
        region.setName("cn-north-1");
        region.setArea("Asia");
        regions = List.of(region);

        DeployVariable deployVariable = new DeployVariable();
        deployVariable.setName("HuaweiCloud AK");
        variables = List.of(deployVariable);

        flavors = new Flavors();
        ServiceFlavor flavor = new ServiceFlavor();
        flavor.setName("flavor");
        flavors.setServiceFlavors(List.of(flavor));

        billing = new Billing();
        billing.setBillingModes(Arrays.asList(BillingMode.values()));

        serviceTemplateDetailVo = new ServiceTemplateDetailVo();
        serviceTemplateDetailVo.setId(uuid);
        serviceTemplateDetailVo.setCategory(category);
        serviceTemplateDetailVo.setName(name);
        serviceTemplateDetailVo.setVersion(version);
        serviceTemplateDetailVo.setCsp(csp);
        serviceTemplateDetailVo.setRegions(regions);
        serviceTemplateDetailVo.setDescription(description);
        serviceTemplateDetailVo.setNamespace(namespace);
        serviceTemplateDetailVo.setIcon(icon);
        serviceTemplateDetailVo.setDeployment(DEPLOYMENT);
        serviceTemplateDetailVo.setVariables(variables);
        serviceTemplateDetailVo.setFlavors(flavors);
        serviceTemplateDetailVo.setBilling(billing);
        serviceTemplateDetailVo.setCreateTime(createTime);
        serviceTemplateDetailVo.setLastModifiedTime(lastModifiedTime);
        serviceTemplateDetailVo.setServiceRegistrationState(serviceRegistrationState);
        serviceTemplateDetailVo.setReviewComment(reviewComment);
        serviceTemplateDetailVo.setServiceHostingType(ServiceHostingType.SERVICE_VENDOR);
        serviceTemplateDetailVo.setServiceProviderContactDetails(serviceProviderContactDetails);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(uuid, serviceTemplateDetailVo.getId());
        assertEquals(category, serviceTemplateDetailVo.getCategory());
        assertEquals(name, serviceTemplateDetailVo.getName());
        assertEquals(version, serviceTemplateDetailVo.getVersion());
        assertEquals(csp, serviceTemplateDetailVo.getCsp());
        assertEquals(regions, serviceTemplateDetailVo.getRegions());
        assertEquals(description, serviceTemplateDetailVo.getDescription());
        assertEquals(namespace, serviceTemplateDetailVo.getNamespace());
        assertEquals(icon, serviceTemplateDetailVo.getIcon());
        assertEquals(variables, serviceTemplateDetailVo.getVariables());
        assertEquals(flavors, serviceTemplateDetailVo.getFlavors());
        assertEquals(billing, serviceTemplateDetailVo.getBilling());
        assertEquals(createTime, serviceTemplateDetailVo.getCreateTime());
        assertEquals(lastModifiedTime, serviceTemplateDetailVo.getLastModifiedTime());
        assertEquals(serviceRegistrationState,
                serviceTemplateDetailVo.getServiceRegistrationState());
        assertEquals(reviewComment, serviceTemplateDetailVo.getReviewComment());
        assertEquals(ServiceHostingType.SERVICE_VENDOR,
                serviceTemplateDetailVo.getServiceHostingType());
        assertEquals(serviceProviderContactDetails,
                serviceTemplateDetailVo.getServiceProviderContactDetails());
    }

    @Test
    public void testEqualsAndHashCode() {
        assertNotEquals(serviceTemplateDetailVo, new Object());
        assertNotEquals(serviceTemplateDetailVo.hashCode(), new Object().hashCode());
        ServiceTemplateDetailVo test = new ServiceTemplateDetailVo();
        assertNotEquals(serviceTemplateDetailVo, test);
        assertNotEquals(serviceTemplateDetailVo.hashCode(), test.hashCode());
        ServiceTemplateDetailVo test2 = new ServiceTemplateDetailVo();
        BeanUtils.copyProperties(serviceTemplateDetailVo, test2);
        assertEquals(serviceTemplateDetailVo, test2);
        assertEquals(serviceTemplateDetailVo.hashCode(), test2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString = "ServiceTemplateDetailVo(" +
                "id=" + uuid + ", " +
                "name=" + name + ", " +
                "version=" + version + ", " +
                "csp=" + csp + ", " +
                "category=" + category + ", " +
                "namespace=" + namespace + ", " +
                "regions=" + regions + ", " +
                "description=" + description + ", " +
                "icon=" + icon + ", " +
                "deployment=" + DEPLOYMENT + ", " +
                "variables=" + variables + ", " +
                "flavors=" + flavors + ", " +
                "billing=" + billing + ", " +
                "serviceHostingType=" + ServiceHostingType.SERVICE_VENDOR + ", " +
                "createTime=" + createTime + ", " +
                "lastModifiedTime=" + lastModifiedTime + ", " +
                "serviceRegistrationState=" + serviceRegistrationState + ", " +
                "reviewComment=" + reviewComment + ", " +
                "serviceProviderContactDetails=" + serviceProviderContactDetails + ")";

        assertEquals(expectedToString, serviceTemplateDetailVo.toString());
    }

}