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
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of ServiceDetailVo.
 */
class ServiceDetailVoTest {
    private static final String userId = "userId";
    private static final Category category = Category.COMPUTE;
    private static final String serviceName = "serviceName";
    private static final String version = "v1.0.0";
    private static final String region = "us-east-1";
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
    private static ServiceDetailVo serviceDetailVo;

    @BeforeEach
    void setUp() {
        deployRequest = new DeployRequest();
        deployRequest.setUserId(userId);
        deployRequest.setCategory(category);
        deployRequest.setServiceName(serviceName);
        deployRequest.setVersion(version);
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

        serviceDetailVo = new ServiceDetailVo();
        serviceDetailVo.setDeployRequest(deployRequest);
        serviceDetailVo.setDeployResources(deployResources);
        serviceDetailVo.setDeployedServiceProperties(deployedServiceProperties);
        serviceDetailVo.setResultMessage(resultSuccessMessage);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(userId, deployRequest.getUserId());
        assertEquals(category, deployRequest.getCategory());
        assertEquals(serviceName, deployRequest.getServiceName());
        assertEquals(version, deployRequest.getVersion());
        assertEquals(region, deployRequest.getRegion());
        assertEquals(csp, deployRequest.getCsp());
        assertEquals(flavor, deployRequest.getFlavor());
        assertEquals(customerServiceName, deployRequest.getCustomerServiceName());
        assertEquals(properties, deployRequest.getServiceRequestProperties());

        assertEquals(uuid.toString(), deployResource.getResourceId());
        assertEquals(name, deployResource.getName());
        assertEquals(kind, deployResource.getKind());
        assertEquals(Collections.emptyMap(), deployResource.getProperties());

        assertEquals(deployRequest, serviceDetailVo.getDeployRequest());
        assertEquals(deployResources, serviceDetailVo.getDeployResources());
        assertEquals(deployedServiceProperties, serviceDetailVo.getDeployedServiceProperties());
        assertEquals(resultSuccessMessage, serviceDetailVo.getResultMessage());
    }

    @Test
    void testEqualsAndHashCode() {
        assertEquals(serviceDetailVo.hashCode(), serviceDetailVo.hashCode());

        Object obj = new Object();
        assertNotEquals(serviceDetailVo, obj);
        assertNotEquals(null, serviceDetailVo);
        assertNotEquals(serviceDetailVo.hashCode(), obj.hashCode());

        ServiceDetailVo serviceDetailVo1 = new ServiceDetailVo();
        ServiceDetailVo serviceDetailVo2 = new ServiceDetailVo();
        assertNotEquals(serviceDetailVo, serviceDetailVo1);
        assertNotEquals(serviceDetailVo, serviceDetailVo2);
        assertEquals(serviceDetailVo1, serviceDetailVo2);
        assertNotEquals(serviceDetailVo.hashCode(), serviceDetailVo1.hashCode());
        assertNotEquals(serviceDetailVo.hashCode(), serviceDetailVo2.hashCode());
        assertEquals(serviceDetailVo1.hashCode(), serviceDetailVo2.hashCode());

        serviceDetailVo1.setDeployRequest(deployRequest);
        assertNotEquals(serviceDetailVo, serviceDetailVo1);
        assertNotEquals(serviceDetailVo1, serviceDetailVo2);
        assertNotEquals(serviceDetailVo.hashCode(), serviceDetailVo1.hashCode());
        assertNotEquals(serviceDetailVo1.hashCode(), serviceDetailVo2.hashCode());

        serviceDetailVo1.setDeployResources(deployResources);
        assertNotEquals(serviceDetailVo, serviceDetailVo1);
        assertNotEquals(serviceDetailVo1, serviceDetailVo2);
        assertNotEquals(serviceDetailVo.hashCode(), serviceDetailVo1.hashCode());
        assertNotEquals(serviceDetailVo1.hashCode(), serviceDetailVo2.hashCode());

        serviceDetailVo1.setDeployedServiceProperties(deployedServiceProperties);
        assertNotEquals(serviceDetailVo, serviceDetailVo1);
        assertNotEquals(serviceDetailVo1, serviceDetailVo2);
        assertNotEquals(serviceDetailVo.hashCode(), serviceDetailVo1.hashCode());
        assertNotEquals(serviceDetailVo1.hashCode(), serviceDetailVo2.hashCode());

        serviceDetailVo1.setResultMessage(resultSuccessMessage);
        assertEquals(serviceDetailVo, serviceDetailVo1);
        assertNotEquals(serviceDetailVo1, serviceDetailVo2);
        assertEquals(serviceDetailVo.hashCode(), serviceDetailVo1.hashCode());
        assertNotEquals(serviceDetailVo1.hashCode(), serviceDetailVo2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString = "ServiceDetailVo(deployRequest=" + deployRequest +
                ", deployResources=" + deployResources +
                ", deployedServiceProperties=" + deployedServiceProperties +
                ", resultMessage=" + resultSuccessMessage + ")";

        assertEquals(expectedToString, serviceDetailVo.toString());
    }

}