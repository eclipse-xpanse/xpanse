/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.database.mariadb;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URL;
import java.util.Objects;
import java.util.UUID;
import org.eclipse.xpanse.api.ServiceRegisterApi;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.eclipse.xpanse.modules.models.service.register.exceptions.ServiceAlreadyRegisteredException;
import org.eclipse.xpanse.modules.models.service.utils.OclLoader;
import org.eclipse.xpanse.modules.models.service.view.RegisteredServiceVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RegistrationWithMariaDbTest extends AbstractMariaDbIntegrationTest {

    @Autowired
    private ServiceRegisterApi serviceRegisterApi;

    @Autowired
    private OclLoader oclLoader;

    @Test
    void testRegisterNewService() throws Exception {
        RegisteredServiceVo registeredServiceVo = serviceRegisterApi.register(getOclFromFile());
        Assertions.assertTrue(Objects.nonNull(registeredServiceVo));
        Assertions.assertEquals(1, serviceRegisterApi.listRegisteredServices(
                null, null, null, null
        ).stream().filter(registeredServiceVo1 -> registeredServiceVo1.getName()
                .equals(registeredServiceVo.getName())).toList().size());
        Assertions.assertEquals("v3.3.2",
                serviceRegisterApi.detail(registeredServiceVo.getId().toString()).getOcl()
                        .getServiceVersion());
    }

    @Test
    void testRegisterUniqueValidation() throws Exception {
        Ocl ocl = getOclFromFile();
        serviceRegisterApi.register(ocl);
        assertThrows(ServiceAlreadyRegisteredException.class,
                () -> serviceRegisterApi.register(ocl));
    }

    @Test
    void testServiceRegistrationUpdate() throws Exception {
        RegisteredServiceVo registeredServiceVo = serviceRegisterApi.register(getOclFromFile());
        ;
        registeredServiceVo.getOcl().setVersion("v3.3.3");
        serviceRegisterApi.update(registeredServiceVo.getId().toString(),
                registeredServiceVo.getOcl());
        Assertions.assertEquals(1, serviceRegisterApi.listRegisteredServices(
                null, null, null, null
        ).stream().filter(registeredServiceVo1 -> registeredServiceVo1.getName()
                .equals(registeredServiceVo.getName())).toList().size());
        Assertions.assertEquals("v3.3.3",
                serviceRegisterApi.detail(registeredServiceVo.getId().toString()).getOcl()
                        .getVersion());
    }

    private Ocl getOclFromFile() throws Exception {
        Ocl ocl = oclLoader.getOcl(new URL("file:src/test/resources/ocl_test_dummy.yaml"));
        ocl.setName(UUID.randomUUID().toString());
        return ocl;
    }

}
