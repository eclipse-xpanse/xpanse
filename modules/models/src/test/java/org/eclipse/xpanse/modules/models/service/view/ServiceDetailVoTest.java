/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.CreateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceState;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of ServiceDetailVo.
 */
class ServiceDetailVoTest {

    private static CreateRequest createRequest;
    private static List<@Valid DeployResource> deployResources;
    private static Map<String, String> deployedServiceProperties;
    private static String resultSuccessMessage = "Deployment successful";
    private static String resultFailMessage = "Deployment failed";

    @BeforeEach
    void setUp() {
        createRequest = new CreateRequest();
        deployResources = Collections.singletonList(new DeployResource());
        deployedServiceProperties = new HashMap<>();
    }

    @Test
    public void testGetterAndSetter() {
        ServiceDetailVo serviceDetailVo = new ServiceDetailVo();
        serviceDetailVo.setCreateRequest(createRequest);
        serviceDetailVo.setDeployResources(deployResources);
        serviceDetailVo.setDeployedServiceProperties(deployedServiceProperties);
        serviceDetailVo.setResultMessage(resultSuccessMessage);

        assertEquals(createRequest, serviceDetailVo.getCreateRequest());
        assertEquals(deployResources, serviceDetailVo.getDeployResources());
        assertEquals(deployedServiceProperties, serviceDetailVo.getDeployedServiceProperties());
        assertEquals(resultSuccessMessage, serviceDetailVo.getResultMessage());
    }

    @Test
    public void testEqualsAndHashCode() {
        ServiceDetailVo serviceDetailVo1 = new ServiceDetailVo();
        serviceDetailVo1.setCreateRequest(createRequest);
        serviceDetailVo1.setDeployResources(deployResources);
        serviceDetailVo1.setDeployedServiceProperties(deployedServiceProperties);
        serviceDetailVo1.setResultMessage(resultSuccessMessage);

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

        Assertions.assertEquals(serviceDetailVo1, serviceDetailVo2);
        assertEquals(serviceDetailVo1.hashCode(), serviceDetailVo2.hashCode());
        Assertions.assertNotEquals(serviceDetailVo1, serviceDetailVo3);
        Assertions.assertNotEquals(serviceDetailVo1.hashCode(),
                serviceDetailVo3.hashCode());
    }
}
