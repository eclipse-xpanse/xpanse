/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of DeployedServiceDetails.
 */
class DeployedServiceDetailsTest {
    private static final String userId = "userId";
    private static final Category category = Category.COMPUTE;
    private static final String serviceName = "serviceName";
    private static final String version = "1.0.0";
    private static final String regionName = "us-east-1";
    private static final String areaName = "Asia China";
    private static final Csp csp = Csp.AWS;
    private static final String flavor = "basic";
    private static final String customerServiceName = "customerServiceName";
    private static final UUID uuid = UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5");
    private static final String name = "resource";
    private static final DeployResourceKind kind = DeployResourceKind.VM;
    private static final String resultSuccessMessage = "Deployment successful";
    private static DeployResource deployResource;
    private static Map<String, Object> properties;
    private static DeployRequest deployRequest;
    private static List<@Valid DeployResource> deployResources;
    private static Map<String, String> deployedServiceProperties;
    private static DeployedServiceDetails deployedServiceDetails;

    @BeforeEach
    void setUp() {
        deployRequest = new DeployRequest();
        deployRequest.setUserId(userId);
        deployRequest.setCategory(category);
        deployRequest.setServiceName(serviceName);
        deployRequest.setVersion(version);
        Region region = new Region();
        region.setName(regionName);
        region.setArea(areaName);
        deployRequest.setRegion(region);
        deployRequest.setCsp(csp);
        deployRequest.setFlavor(flavor);
        deployRequest.setCustomerServiceName(customerServiceName);
        properties = new HashMap<>();
        properties.put("key", "value");
        deployRequest.setServiceRequestProperties(properties);

        deployResources = new ArrayList<>();
        deployResource = new DeployResource();
        deployResource.setResourceId(uuid.toString());
        deployResource.setName(name);
        deployResource.setKind(kind);
        deployResource.setProperties(new HashMap<>());
        deployResources.add(deployResource);

        deployedServiceProperties = new HashMap<>();
        deployedServiceProperties.put("key1", "value1");
        deployedServiceProperties.put("key2", "value2");

        deployedServiceDetails = new DeployedServiceDetails();
        deployedServiceDetails.setDeployRequest(deployRequest);
        deployedServiceDetails.setDeployResources(deployResources);
        deployedServiceDetails.setDeployedServiceProperties(deployedServiceProperties);
        deployedServiceDetails.setResultMessage(resultSuccessMessage);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(userId, deployRequest.getUserId());
        assertEquals(category, deployRequest.getCategory());
        assertEquals(serviceName, deployRequest.getServiceName());
        assertEquals(version, deployRequest.getVersion());
        Region region = new Region();
        region.setName(regionName);
        region.setArea(areaName);
        assertEquals(region, deployRequest.getRegion());
        assertEquals(csp, deployRequest.getCsp());
        assertEquals(flavor, deployRequest.getFlavor());
        assertEquals(customerServiceName, deployRequest.getCustomerServiceName());
        assertEquals(properties, deployRequest.getServiceRequestProperties());

        assertEquals(uuid.toString(), deployResource.getResourceId());
        assertEquals(name, deployResource.getName());
        assertEquals(kind, deployResource.getKind());
        assertEquals(Collections.emptyMap(), deployResource.getProperties());

        assertEquals(deployRequest, deployedServiceDetails.getDeployRequest());
        assertEquals(deployResources, deployedServiceDetails.getDeployResources());
        assertEquals(deployedServiceProperties, deployedServiceDetails.getDeployedServiceProperties());
        assertEquals(resultSuccessMessage, deployedServiceDetails.getResultMessage());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(deployedServiceDetails.hashCode(), deployedServiceDetails.hashCode());

        Object obj = new Object();
        assertNotEquals(deployedServiceDetails, obj);
        assertNotEquals(null, deployedServiceDetails);
        assertNotEquals(deployedServiceDetails.hashCode(), obj.hashCode());

        DeployedServiceDetails deployedServiceDetails1 = new DeployedServiceDetails();
        DeployedServiceDetails deployedServiceDetails2 = new DeployedServiceDetails();
        assertNotEquals(deployedServiceDetails, deployedServiceDetails1);
        assertNotEquals(deployedServiceDetails, deployedServiceDetails2);
        assertEquals(deployedServiceDetails1, deployedServiceDetails2);
        assertNotEquals(deployedServiceDetails.hashCode(), deployedServiceDetails1.hashCode());
        assertNotEquals(deployedServiceDetails.hashCode(), deployedServiceDetails2.hashCode());
        assertEquals(deployedServiceDetails1.hashCode(), deployedServiceDetails2.hashCode());

        deployedServiceDetails1.setDeployRequest(deployRequest);
        assertNotEquals(deployedServiceDetails, deployedServiceDetails1);
        assertNotEquals(deployedServiceDetails1, deployedServiceDetails2);
        assertNotEquals(deployedServiceDetails.hashCode(), deployedServiceDetails1.hashCode());
        assertNotEquals(deployedServiceDetails1.hashCode(), deployedServiceDetails2.hashCode());

        deployedServiceDetails1.setDeployResources(deployResources);
        assertNotEquals(deployedServiceDetails, deployedServiceDetails1);
        assertNotEquals(deployedServiceDetails1, deployedServiceDetails2);
        assertNotEquals(deployedServiceDetails.hashCode(), deployedServiceDetails1.hashCode());
        assertNotEquals(deployedServiceDetails1.hashCode(), deployedServiceDetails2.hashCode());

        deployedServiceDetails1.setDeployedServiceProperties(deployedServiceProperties);
        assertNotEquals(deployedServiceDetails, deployedServiceDetails1);
        assertNotEquals(deployedServiceDetails1, deployedServiceDetails2);
        assertNotEquals(deployedServiceDetails.hashCode(), deployedServiceDetails1.hashCode());
        assertNotEquals(deployedServiceDetails1.hashCode(), deployedServiceDetails2.hashCode());

        deployedServiceDetails1.setResultMessage(resultSuccessMessage);
        assertEquals(deployedServiceDetails, deployedServiceDetails1);
        assertNotEquals(deployedServiceDetails1, deployedServiceDetails2);
        assertEquals(deployedServiceDetails.hashCode(), deployedServiceDetails1.hashCode());
        assertNotEquals(deployedServiceDetails1.hashCode(), deployedServiceDetails2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString = "DeployedServiceDetails(deployRequest=" + deployRequest +
                ", deployResources=" + deployResources +
                ", deployedServiceProperties=" + deployedServiceProperties +
                ", resultMessage=" + resultSuccessMessage + ")";

        assertEquals(expectedToString, deployedServiceDetails.toString());
    }

}