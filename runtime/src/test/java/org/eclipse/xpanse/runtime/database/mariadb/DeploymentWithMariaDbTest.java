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
import org.eclipse.xpanse.api.controllers.ServiceDeployerApi;
import org.eclipse.xpanse.api.controllers.ServiceTemplateApi;
import org.eclipse.xpanse.modules.models.service.deploy.CreateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.utils.ServiceVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.service.view.ServiceDetailVo;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


class DeploymentWithMariaDbTest extends AbstractMariaDbIntegrationTest {

    @Autowired
    ServiceDeployerApi serviceDeployerApi;

    @Autowired
    ServiceTemplateApi ServiceTemplateApi;

    @Autowired
    ServiceVariablesJsonSchemaGenerator serviceVariablesJsonSchemaGenerator;

    @Autowired
    OclLoader oclLoader;

    @Test
    void testServiceDeployment() throws Exception {
        ServiceTemplateVo serviceTemplate = registerService();
        CreateRequest createRequest = new CreateRequest();
        createRequest.setUserId("userId");
        createRequest.setServiceName(serviceTemplate.getName());
        createRequest.setVersion(serviceTemplate.getVersion());
        createRequest.setCsp(serviceTemplate.getCsp());
        createRequest.setCategory(serviceTemplate.getCategory());
        createRequest.setFlavor(serviceTemplate.getOcl().getFlavors().get(0).toString());
        createRequest.setRegion(
                serviceTemplate.getOcl().getCloudServiceProvider().getRegions().get(0)
                        .toString());
        Map<String, String> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("admin_passwd", "111111111@Qq");
        createRequest.setServiceRequestProperties(serviceRequestProperties);

        UUID deployUUid = serviceDeployerApi.deploy(createRequest);
        await().ignoreException(ServiceNotDeployedException.class)
                .until(() -> serviceDeployerApi.getServiceDetailsById(deployUUid.toString())
                        != null);
        ServiceDetailVo serviceDetailVo =
                serviceDeployerApi.getServiceDetailsById(deployUUid.toString());
        Assertions.assertNotNull(serviceDetailVo);
        Assertions.assertEquals(ServiceDeploymentState.DEPLOY_FAILED,
                serviceDetailVo.getServiceDeploymentState());
    }

    private ServiceTemplateVo registerService() throws Exception {
        Ocl ocl = oclLoader.getOcl(new URL("file:src/test/resources/ocl_test_dummy.yaml"));
        return ServiceTemplateApi.register(ocl);
    }
}
