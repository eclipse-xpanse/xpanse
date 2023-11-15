/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.database.mysql;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.api.controllers.ServiceDeployerApi;
import org.eclipse.xpanse.api.controllers.ServiceTemplateApi;
import org.eclipse.xpanse.modules.models.security.constant.RoleConstants;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.utils.ServiceVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=zitadel,mysql,zitadel-testbed"})
@AutoConfigureMockMvc
class DeploymentWithMysqlTest extends AbstractMysqlIntegrationTest {

    @Autowired
    ServiceDeployerApi serviceDeployerApi;

    @Autowired
    ServiceTemplateApi ServiceTemplateApi;

    @Autowired
    ServiceVariablesJsonSchemaGenerator serviceVariablesJsonSchemaGenerator;

    @Autowired
    OclLoader oclLoader;

    @Test
    @WithMockJwtAuth(authorities = {RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_ISV,
            RoleConstants.ROLE_USER},
            claims = @OpenIdClaims(sub = "adminId", preferredUsername = "adminName"))
    void testServiceDeployment() throws Exception {
        ServiceTemplateDetailVo serviceTemplate = registerService();
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setUserId("userId");
        deployRequest.setServiceName(serviceTemplate.getName());
        deployRequest.setVersion(serviceTemplate.getVersion());
        deployRequest.setCsp(serviceTemplate.getCsp());
        deployRequest.setCategory(serviceTemplate.getCategory());
        deployRequest.setFlavor(serviceTemplate.getFlavors().get(0).toString());
        deployRequest.setRegion(serviceTemplate.getRegions().get(0).toString());
        deployRequest.setServiceHostingType(ServiceHostingType.SELF);
        Map<String, Object> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("admin_passwd", "111111111@Qq");
        deployRequest.setServiceRequestProperties(serviceRequestProperties);

        UUID deployUUid = serviceDeployerApi.deploy(deployRequest);
        Assertions.assertThrows(ServiceNotDeployedException.class,
                () -> serviceDeployerApi.getSelfHostedServiceDetailsById(deployUUid.toString()));

    }

    private ServiceTemplateDetailVo registerService() throws Exception {
        Ocl ocl =
                oclLoader.getOcl(URI.create("file:src/test/resources/ocl_test_dummy.yaml").toURL());
        return ServiceTemplateApi.register(ocl);
    }
}
