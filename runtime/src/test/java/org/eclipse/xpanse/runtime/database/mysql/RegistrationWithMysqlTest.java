/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.database.mysql;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockJwtAuth;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.controllers.ServiceTemplateApi;
import org.eclipse.xpanse.modules.models.security.constant.RoleConstants;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateAlreadyRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=zitadel,mysql,zitadel-testbed"})
@AutoConfigureMockMvc
class RegistrationWithMysqlTest extends AbstractMysqlIntegrationTest {

    @Autowired
    private ServiceTemplateApi serviceTemplateApi;

    @Autowired
    private OclLoader oclLoader;

    @Test
    @WithMockJwtAuth(authorities = RoleConstants.ROLE_ISV,
            claims = @OpenIdClaims(sub = "isvUserId", preferredUsername = "isvUserName"))
    void testRegisterNewService() throws Exception {
        ServiceTemplateVo serviceTemplateVo = serviceTemplateApi.register(getOclFromFile());
        Assertions.assertTrue(Objects.nonNull(serviceTemplateVo));
        Assertions.assertEquals(1, serviceTemplateApi.listServiceTemplates(
                null, null, null, null
        ).stream().filter(registeredServiceVo1 -> registeredServiceVo1.getName()
                .equals(serviceTemplateVo.getName())).toList().size());
        Assertions.assertEquals("v3.3.2",
                serviceTemplateApi.details(serviceTemplateVo.getId().toString()).getOcl()
                        .getServiceVersion());
    }

    @Test
    @WithMockJwtAuth(authorities = RoleConstants.ROLE_ADMIN,
            claims = @OpenIdClaims(sub = "isvUserId", preferredUsername = "isvUserName"))
    void testRegisterUniqueValidation() throws Exception {
        Ocl ocl = getOclFromFile();
        serviceTemplateApi.register(ocl);
        assertThrows(ServiceTemplateAlreadyRegistered.class,
                () -> serviceTemplateApi.register(ocl));
    }

    @Test
    @WithMockJwtAuth(authorities = RoleConstants.ROLE_ISV,
            claims = @OpenIdClaims(sub = "isvUserId", preferredUsername = "isvUserName"))
    void testServiceRegistrationUpdate() throws Exception {
        ServiceTemplateVo serviceTemplateVo = serviceTemplateApi.register(getOclFromFile());
        serviceTemplateVo.getOcl().setVersion("v3.3.3");
        serviceTemplateApi.update(serviceTemplateVo.getId().toString(),
                serviceTemplateVo.getOcl());
        Assertions.assertEquals(1, serviceTemplateApi.listServiceTemplates(
                null, null, null, null
        ).stream().filter(registeredServiceVo1 -> registeredServiceVo1.getName()
                .equals(serviceTemplateVo.getName())).toList().size());
        Assertions.assertEquals("v3.3.3",
                serviceTemplateApi.details(serviceTemplateVo.getId().toString()).getOcl()
                        .getVersion());
    }

    private Ocl getOclFromFile() throws Exception {
        Ocl ocl = oclLoader.getOcl(new URL("file:src/test/resources/ocl_test_dummy.yaml"));
        ocl.setName(UUID.randomUUID().toString());
        return ocl;
    }

}
