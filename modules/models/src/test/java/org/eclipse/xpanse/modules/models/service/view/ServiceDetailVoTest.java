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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.CreateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of ServiceDetailVo.
 */
class ServiceDetailVoTest {

    private static final String resultSuccessMessage = "Deployment successful";
    private static final String resultFailMessage = "Deployment failed";
    private static DeployResource deployResource;
    private static Map<String, String> properties;
    private static CreateRequest createRequest;
    private static List<@Valid DeployResource> deployResources;
    private static Map<String, String> deployedServiceProperties;
    private static ServiceDetailVo serviceDetailVo;

    @BeforeEach
    void setUp() {
        createRequest = new CreateRequest();
        createRequest.setUserName("user");
        createRequest.setCategory(Category.COMPUTE);
        createRequest.setServiceName("serviceName");
        createRequest.setVersion("v1.0.0");
        createRequest.setRegion("us-east-1");
        createRequest.setCsp(Csp.AWS);
        createRequest.setFlavor("basic");
        createRequest.setCustomerServiceName("customerServiceName");
        properties = new HashMap<>();
        properties.put("key", "value");
        createRequest.setServiceRequestProperties(properties);

        deployResources = new ArrayList<>();
        deployResource = new DeployResource();
        deployResource.setResourceId("20424910-5f64-4984-84f0-6013c63c64f5");
        deployResource.setName("resource");
        deployResource.setKind(DeployResourceKind.VM);
        deployResource.setProperties(properties);
        deployResources.add(deployResource);

        deployedServiceProperties = new HashMap<>();
        deployedServiceProperties.put("key1", "value1");
        deployedServiceProperties.put("key2", "value2");

        serviceDetailVo = new ServiceDetailVo();
        serviceDetailVo.setCreateRequest(createRequest);
        serviceDetailVo.setDeployResources(deployResources);
        serviceDetailVo.setDeployedServiceProperties(deployedServiceProperties);
        serviceDetailVo.setResultMessage(resultSuccessMessage);
    }

    @Test
    public void testGetterAndSetter() {
        assertEquals("user", createRequest.getUserName());
        assertEquals(Category.COMPUTE, createRequest.getCategory());
        assertEquals("serviceName", createRequest.getServiceName());
        assertEquals("v1.0.0", createRequest.getVersion());
        assertEquals("us-east-1", createRequest.getRegion());
        assertEquals(Csp.AWS, createRequest.getCsp());
        assertEquals("basic", createRequest.getFlavor());
        assertEquals("customerServiceName", createRequest.getCustomerServiceName());
        assertEquals(properties, createRequest.getServiceRequestProperties());

        assertEquals("20424910-5f64-4984-84f0-6013c63c64f5", deployResource.getResourceId());
        assertEquals("resource", deployResource.getName());
        assertEquals(DeployResourceKind.VM, deployResource.getKind());
        assertEquals(properties, deployResource.getProperties());

        assertEquals(createRequest, serviceDetailVo.getCreateRequest());
        assertEquals(deployResources, serviceDetailVo.getDeployResources());
        assertEquals(deployedServiceProperties, serviceDetailVo.getDeployedServiceProperties());
        assertEquals(resultSuccessMessage, serviceDetailVo.getResultMessage());
    }

    @Test
    public void testEqualsAndHashCode() {
        ServiceDetailVo serviceDetailVo2 = new ServiceDetailVo();
        serviceDetailVo2.setCreateRequest(createRequest);
        serviceDetailVo2.setDeployResources(deployResources);
        serviceDetailVo2.setDeployedServiceProperties(deployedServiceProperties);
        serviceDetailVo2.setResultMessage(resultSuccessMessage);

        ServiceDetailVo serviceDetailVo3 = new ServiceDetailVo();
        serviceDetailVo3.setCreateRequest(createRequest);
        serviceDetailVo3.setDeployResources(deployResources);
        serviceDetailVo3.setDeployedServiceProperties(deployedServiceProperties);
        serviceDetailVo3.setResultMessage(resultFailMessage);

        assertEquals(serviceDetailVo, serviceDetailVo);
        assertEquals(serviceDetailVo, serviceDetailVo2);
        assertNotEquals(serviceDetailVo, serviceDetailVo3);

        assertEquals(serviceDetailVo.hashCode(), serviceDetailVo.hashCode());
        assertEquals(serviceDetailVo.hashCode(), serviceDetailVo2.hashCode());
        assertNotEquals(serviceDetailVo.hashCode(),
                serviceDetailVo3.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString = "ServiceDetailVo(createRequest=" + createRequest +
                ", deployResources=" + deployResources +
                ", deployedServiceProperties=" + deployedServiceProperties +
                ", resultMessage=" + resultSuccessMessage + ")";

        assertEquals(expectedToString, serviceDetailVo.toString());
    }

}