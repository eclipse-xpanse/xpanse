/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.xpanse.modules.models.billing.Billing;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

/**
 * Test of Ocl.
 */
class OclTest {

    private final Category category = Category.COMPUTE;
    private final String version = "2.0";
    private final String name = "kafka";
    private final String serviceVersion = "1.0.0";
    private final String description = "description";
    private final String namespace = "nameSpace";
    private final String icon = "icon";
    private final ServiceHostingType serviceHostingType = ServiceHostingType.SELF;
    @Mock
    private CloudServiceProvider cloudServiceProvider;
    @Mock
    private Deployment deployment;
    @Mock
    private FlavorsWithPrice flavors;
    @Mock
    private Billing billing;
    @Mock
    private ServiceProviderContactDetails serviceProviderContactDetails;

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
        ocl.setServiceProviderContactDetails(serviceProviderContactDetails);
    }

    @Test
    public void testDeepCopyAnEmptyOcl() {
        Ocl ocl1 = new Ocl();
        Ocl aCopy = ocl1.deepCopy();
        assertNotSame(ocl1, aCopy);
        assertNull(aCopy.getName());
    }

    @Test
    public void testDeepCopy() {
        Ocl aCopy = ocl.deepCopy();
        assertEquals(ocl.getCategory(), aCopy.getCategory());
        assertEquals(ocl.getVersion(), aCopy.getVersion());
        assertEquals(ocl.getName(), aCopy.getName());
        assertEquals(ocl.getServiceVersion(), aCopy.getServiceVersion());
        assertEquals(ocl.getDescription(), aCopy.getDescription());
        assertEquals(ocl.getNamespace(), aCopy.getNamespace());
        assertEquals(ocl.getIcon(), aCopy.getIcon());
        assertEquals(ocl.getCloudServiceProvider(), aCopy.getCloudServiceProvider());
        assertEquals(ocl.getDeployment(), aCopy.getDeployment());
        assertEquals(ocl.getFlavors(), aCopy.getFlavors());
        assertEquals(ocl.getBilling(), aCopy.getBilling());
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
                "Ocl(" + "category=" + category + ", version=" + version + ", name=" + name
                        + ", serviceVersion=" + serviceVersion + ", description=" + description
                        + ", namespace=" + namespace + ", icon=" + icon + ", cloudServiceProvider="
                        + cloudServiceProvider + ", deployment=" + deployment + ", flavors="
                        + flavors + ", billing=" + billing + ", serviceHostingType="
                        + serviceHostingType + ", serviceProviderContactDetails="
                        + serviceProviderContactDetails + ", eula=" + null + ")";
        assertEquals(expectedString, ocl.toString());
    }

}
