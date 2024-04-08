/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.database.mysql;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.controllers.ServiceTemplateApi;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateAlreadyRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.semver4j.Semver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,mysql"})
@AutoConfigureMockMvc
class RegistrationWithMysqlTest extends AbstractMysqlIntegrationTest {

    @Autowired
    private ServiceTemplateApi serviceTemplateApi;

    @Autowired
    private OclLoader oclLoader;

    @Test
    @WithJwt(file = "jwt_isv.json")
    void testRegisterNewService() throws Exception {
        Ocl ocl = getOclFromFile();
        ServiceTemplateDetailVo registeredServiceTemplate =
                serviceTemplateApi.register(ocl);
        Assertions.assertTrue(Objects.nonNull(registeredServiceTemplate));
        Assertions.assertEquals(ocl.getCategory(), registeredServiceTemplate.getCategory());
        Assertions.assertEquals(ocl.getName(), registeredServiceTemplate.getName());
        Assertions.assertEquals(new Semver(ocl.getServiceVersion()).getVersion(),
                registeredServiceTemplate.getVersion());
        Assertions.assertEquals(ocl.getCloudServiceProvider().getName(),
                registeredServiceTemplate.getCsp());
        Assertions.assertEquals(ServiceRegistrationState.APPROVAL_PENDING,
                registeredServiceTemplate.getServiceRegistrationState());

        ServiceTemplateDetailVo serviceTemplateDetail =
                serviceTemplateApi.details(registeredServiceTemplate.getId().toString());

        List<ServiceTemplateDetailVo> serviceTemplates =
                serviceTemplateApi.listServiceTemplates(ocl.getCategory(),
                        ocl.getCloudServiceProvider().getName(), ocl.getName(),
                        ocl.getServiceVersion(), ocl.getServiceHostingType(),
                        ServiceRegistrationState.APPROVAL_PENDING);

        Assertions.assertEquals(1, serviceTemplates.size());
        Assertions.assertEquals(serviceTemplateDetail.getId(),
                serviceTemplates.getFirst().getId());
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
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName(UUID.randomUUID().toString());
        return ocl;
    }

}
