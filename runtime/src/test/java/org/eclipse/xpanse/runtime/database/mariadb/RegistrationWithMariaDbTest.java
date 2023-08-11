/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.database.mariadb;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URL;
import java.util.Objects;
import java.util.UUID;
import org.eclipse.xpanse.api.ServiceTemplateApi;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateAlreadyRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RegistrationWithMariaDbTest extends AbstractMariaDbIntegrationTest {

    @Autowired
    private ServiceTemplateApi serviceTemplateApi;

    @Autowired
    private OclLoader oclLoader;

    @Test
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
    void testRegisterUniqueValidation() throws Exception {
        Ocl ocl = getOclFromFile();
        serviceTemplateApi.register(ocl);
        assertThrows(ServiceTemplateAlreadyRegistered.class,
                () -> serviceTemplateApi.register(ocl));
    }

    @Test
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
