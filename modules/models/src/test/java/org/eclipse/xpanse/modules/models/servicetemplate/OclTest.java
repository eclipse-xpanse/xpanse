/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.eclipse.xpanse.modules.models.billing.Billing;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

/** Test of Ocl. */
class OclTest {

    private final Category category = Category.COMPUTE;
    private final String version = "2.0";
    private final String name = "kafka";
    private final String serviceVersion = "1.0.0";
    private final String description = "description";
    private final String namespace = "nameSpace";
    private final String icon = "icon";
    private final String eula = "eula";
    private final ServiceHostingType serviceHostingType = ServiceHostingType.SELF;
    @Mock private CloudServiceProvider cloudServiceProvider;
    @Mock private Deployment deployment;
    @Mock private FlavorsWithPrice flavors;
    @Mock private Billing billing;
    @Mock private ServiceProviderContactDetails serviceProviderContactDetails;
    @Mock private ServiceChangeManage serviceConfigurationManage;

    private Ocl ocl;

    @BeforeEach
    void setUp() {
        ocl = new Ocl();
        ocl.setCategory(category);
        ocl.setVersion(version);
        ocl.setName(name);
        ocl.setServiceVersion(serviceVersion);
        ocl.setDescription(description);
        ocl.setNamespace(namespace);
        ocl.setIcon(icon);
        ocl.setCloudServiceProvider(cloudServiceProvider);
        ocl.setDeployment(deployment);
        ocl.setFlavors(flavors);
        ocl.setBilling(billing);
        ocl.setServiceHostingType(serviceHostingType);
        ocl.setEula(eula);
        ocl.setServiceProviderContactDetails(serviceProviderContactDetails);
        ocl.setServiceConfigurationManage(serviceConfigurationManage);
    }

    @Test
    void testGetters() {
        assertEquals(category, ocl.getCategory());
        assertEquals(version, ocl.getVersion());
        assertEquals(name, ocl.getName());
        assertEquals(serviceVersion, ocl.getServiceVersion());
        assertEquals(description, ocl.getDescription());
        assertEquals(namespace, ocl.getNamespace());
        assertEquals(icon, ocl.getIcon());
        assertEquals(cloudServiceProvider, ocl.getCloudServiceProvider());
        assertEquals(deployment, ocl.getDeployment());
        assertEquals(billing, ocl.getBilling());
        assertEquals(serviceHostingType, ocl.getServiceHostingType());
        assertEquals(serviceProviderContactDetails, ocl.getServiceProviderContactDetails());
        assertEquals(flavors, ocl.getFlavors());
        assertEquals(eula, ocl.getEula());
        assertEquals(serviceConfigurationManage, ocl.getServiceConfigurationManage());
    }

    @Test
    public void testEqualsAndHashCode() {
        Object obj = new Object();
        assertNotEquals(ocl, obj);
        assertNotEquals(ocl.hashCode(), obj.hashCode());

        Ocl ocl1 = new Ocl();
        assertNotEquals(ocl, ocl1);
        assertNotEquals(ocl.hashCode(), ocl1.hashCode());

        BeanUtils.copyProperties(ocl, ocl1);
        assertEquals(ocl, ocl1);
        assertEquals(ocl.hashCode(), ocl1.hashCode());
    }

    @Test
    void testToString() {
        String expectedString =
                "Ocl("
                        + "category="
                        + category
                        + ", version="
                        + version
                        + ", name="
                        + name
                        + ", serviceVersion="
                        + serviceVersion
                        + ", description="
                        + description
                        + ", namespace="
                        + namespace
                        + ", icon="
                        + icon
                        + ", cloudServiceProvider="
                        + cloudServiceProvider
                        + ", deployment="
                        + deployment
                        + ", flavors="
                        + flavors
                        + ", billing="
                        + billing
                        + ", serviceHostingType="
                        + serviceHostingType
                        + ", serviceProviderContactDetails="
                        + serviceProviderContactDetails
                        + ", eula="
                        + eula
                        + ", "
                        + "serviceConfigurationManage="
                        + serviceConfigurationManage
                        + ")";
        assertEquals(expectedString, ocl.toString());
    }
}
