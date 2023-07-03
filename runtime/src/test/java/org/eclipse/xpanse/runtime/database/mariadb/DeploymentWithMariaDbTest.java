/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.database.mariadb;

import static org.awaitility.Awaitility.await;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.api.ServiceDeployerApi;
import org.eclipse.xpanse.api.ServiceRegisterApi;
import org.eclipse.xpanse.modules.models.service.deploy.CreateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.eclipse.xpanse.modules.models.service.utils.OclLoader;
import org.eclipse.xpanse.modules.models.service.view.RegisteredServiceVo;
import org.eclipse.xpanse.modules.models.service.view.ServiceDetailVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


class DeploymentWithMariaDbTest extends AbstractMariaDbIntegrationTest {

    @Autowired
    ServiceDeployerApi serviceDeployerApi;

    @Autowired
    ServiceRegisterApi serviceRegisterApi;

    @Autowired
    OclLoader oclLoader;

    @Test
    void testServiceDeployment() throws Exception {
        RegisteredServiceVo registeredServiceVo = registerService();
        CreateRequest createRequest = new CreateRequest();
        createRequest.setUserName("admin");
        createRequest.setServiceName(registeredServiceVo.getName());
        createRequest.setVersion(registeredServiceVo.getVersion());
        createRequest.setCsp(registeredServiceVo.getCsp());
        createRequest.setCategory(registeredServiceVo.getCategory());
        createRequest.setFlavor(registeredServiceVo.getOcl().getFlavors().get(0).toString());
        createRequest.setRegion(
                registeredServiceVo.getOcl().getCloudServiceProvider().getRegions().get(0)
                        .toString());
        Map<String, String> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("secgroup_id", "e2d4de73-1518-40f7-8de1-60f184ea6e1d");
        createRequest.setServiceRequestProperties(serviceRequestProperties);

        UUID deployUUid = serviceDeployerApi.deploy(createRequest);
        await()
                .ignoreException(ServiceNotDeployedException.class)
                .until(() -> serviceDeployerApi.getDeployedServiceDetailsById(deployUUid.toString(),
                        createRequest.getUserName()) != null);
        ServiceDetailVo serviceDetailVo =
                serviceDeployerApi.getDeployedServiceDetailsById(deployUUid.toString(),
                        createRequest.getUserName());
        Assertions.assertNotNull(serviceDetailVo);
        Assertions.assertEquals(ServiceDeploymentState.DEPLOY_FAILED,
                serviceDetailVo.getServiceDeploymentState());
    }

    private RegisteredServiceVo registerService() throws Exception {
        Ocl ocl = oclLoader.getOcl(new URL("file:src/test/resources/ocl_test_dummy.yaml"));
        return serviceRegisterApi.register(ocl);
    }
}
