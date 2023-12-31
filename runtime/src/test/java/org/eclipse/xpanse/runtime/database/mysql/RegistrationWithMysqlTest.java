/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.database.mysql;

import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_ISV;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockAuthentication;
import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.controllers.ServiceTemplateApi;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateAlreadyRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
    @WithMockAuthentication(authType = JwtAuthenticationToken.class)
    void testRegisterNewService() throws Exception {
        super.updateJwtInSecurityContext(Collections.emptyMap(), Collections.singletonList(ROLE_ISV));
        ServiceTemplateDetailVo serviceTemplateDetailVo =
                serviceTemplateApi.register(getOclFromFile());
        Assertions.assertTrue(Objects.nonNull(serviceTemplateDetailVo));
        Assertions.assertEquals(1, serviceTemplateApi.listServiceTemplates(
                null, null, null, null, null
        ).stream().filter(registeredServiceVo1 -> registeredServiceVo1.getName()
                .equals(serviceTemplateDetailVo.getName())).toList().size());
        Assertions.assertEquals("v3.3.2",
                serviceTemplateApi.details(serviceTemplateDetailVo.getId().toString())
                        .getVersion());
    }

    @Test
    @WithMockAuthentication(authType = JwtAuthenticationToken.class)
    void testRegisterUniqueValidation() throws Exception {
        super.updateJwtInSecurityContext(Collections.emptyMap(), Collections.singletonList(ROLE_ISV));
        Ocl ocl = getOclFromFile();
        serviceTemplateApi.register(ocl);
        assertThrows(ServiceTemplateAlreadyRegistered.class,
                () -> serviceTemplateApi.register(ocl));
    }

    @Test
    @WithMockAuthentication(authType = JwtAuthenticationToken.class)
    void testServiceRegistrationUpdate() throws Exception {
        super.updateJwtInSecurityContext(Collections.emptyMap(), Collections.singletonList(ROLE_ISV));
        Ocl ocl = getOclFromFile();
        ServiceTemplateDetailVo serviceTemplateDetailVo =
                serviceTemplateApi.register(ocl);
        ocl.setDescription("Hello");
        ServiceTemplateDetailVo updatedServiceTemplateDetailVo =
                serviceTemplateApi.update(serviceTemplateDetailVo.getId().toString(), ocl);
        Assertions.assertEquals(1, serviceTemplateApi.listServiceTemplates(
                null, null, null, null, null
        ).stream().filter(registeredServiceVo1 -> registeredServiceVo1.getName()
                .equals(serviceTemplateDetailVo.getName())).toList().size());
        Assertions.assertEquals("Hello",
                serviceTemplateApi.details(updatedServiceTemplateDetailVo.getId().toString())
                        .getDescription());
    }

    private Ocl getOclFromFile() throws Exception {
        Ocl ocl =
                oclLoader.getOcl(URI.create("file:src/test/resources/ocl_test_dummy.yaml").toURL());
        ocl.setName(UUID.randomUUID().toString());
        return ocl;
    }

}
