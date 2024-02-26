/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.database.mysql;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import java.net.URI;
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
    @WithJwt(file = "jwt_isv.json")
    void testRegisterNewService() throws Exception {
        ServiceTemplateDetailVo serviceTemplateDetailVo =
                serviceTemplateApi.register(getOclFromFile());
        Assertions.assertTrue(Objects.nonNull(serviceTemplateDetailVo));

        Assertions.assertEquals(1, serviceTemplateApi.listServiceTemplates(
                null, null, null, null, null, null
        ).stream().filter(registeredServiceVo1 -> registeredServiceVo1.getName()
                .equals(serviceTemplateDetailVo.getName())).toList().size());
        Assertions.assertEquals("v3.3.2",
                serviceTemplateApi.details(serviceTemplateDetailVo.getId().toString())
                        .getVersion());
    }

    @Test
    @WithJwt(file = "jwt_isv.json")
    void testRegisterUniqueValidation() throws Exception {
        Ocl ocl = getOclFromFile();
        serviceTemplateApi.register(ocl);
        assertThrows(ServiceTemplateAlreadyRegistered.class,
                () -> serviceTemplateApi.register(ocl));
    }

    @Test
    @WithJwt(file = "jwt_isv.json")
    void testServiceRegistrationUpdate() throws Exception {
        Ocl ocl = getOclFromFile();
        ServiceTemplateDetailVo serviceTemplateDetailVo =
                serviceTemplateApi.register(ocl);
        ocl.setDescription("Hello");
        ServiceTemplateDetailVo updatedServiceTemplateDetailVo =
                serviceTemplateApi.update(serviceTemplateDetailVo.getId().toString(), ocl);
        Assertions.assertEquals(1, serviceTemplateApi.listServiceTemplates(
                null, null, null, null, null, null
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
