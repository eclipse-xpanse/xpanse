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
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test of DeployedServiceDetails. */
class DeployedServiceDetailsTest {
    private final String userId = "userId";
    private final Category category = Category.COMPUTE;
    private final String serviceName = "serviceName";
    private final String version = "1.0.0";
    private final String regionName = "us-east-1";
    private final String areaName = "Asia China";
    private final Csp csp = Csp.HUAWEI_CLOUD;
    private final String flavor = "basic";
    private final String customerServiceName = "customerServiceName";
    private final String groupType = "huaweicloud_compute_instance";
    private final String groupName = "compute";
    private final UUID uuid = UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5");
    private final String resourceName = "resource";
    private final DeployResourceKind resourceKind = DeployResourceKind.VM;
    private final String resultSuccessMessage = "Deployment successful";
    private DeployResource deployResource;
    private Map<String, Object> properties;
    private DeployRequest deployRequest;
    private List<@Valid DeployResource> deployResources;
    private Map<String, String> deployedServiceProperties;
    private DeployedServiceDetails deployedServiceDetails;

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
        deployResource.setGroupType(groupType);
        deployResource.setGroupName(groupName);
        deployResource.setResourceName(resourceName);
        deployResource.setResourceId(uuid.toString());
        deployResource.setResourceName(resourceName);
        deployResource.setResourceKind(resourceKind);
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

        assertEquals(groupType, deployResource.getGroupType());
        assertEquals(groupName, deployResource.getGroupName());
        assertEquals(uuid.toString(), deployResource.getResourceId());
        assertEquals(resourceName, deployResource.getResourceName());
        assertEquals(resourceKind, deployResource.getResourceKind());
        assertEquals(Collections.emptyMap(), deployResource.getProperties());

        assertEquals(deployRequest, deployedServiceDetails.getDeployRequest());
        assertEquals(deployResources, deployedServiceDetails.getDeployResources());
        assertEquals(
                deployedServiceProperties, deployedServiceDetails.getDeployedServiceProperties());
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
        String expectedToString =
                "DeployedServiceDetails(deployRequest="
                        + deployRequest
                        + ", deployResources="
                        + deployResources
                        + ", deployedServiceProperties="
                        + deployedServiceProperties
                        + ", resultMessage="
                        + resultSuccessMessage
                        + ")";

        assertEquals(expectedToString, deployedServiceDetails.toString());
    }
}
