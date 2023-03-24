/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.utils;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.models.enums.Category;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.ServiceState;
import org.eclipse.xpanse.modules.models.resource.Ocl;
import org.eclipse.xpanse.modules.models.utils.DeployVariableValidator;
import org.eclipse.xpanse.modules.models.utils.OclLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for OpenApiUtilTest.
 */
@Slf4j
public class OpenApiUtilTest {

    @Test
    public void createServiceApi_tese() throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new URL("file:src/test/resources/ocl_testOpenApi.yaml"));
        RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setId(UUID.randomUUID());
        registerServiceEntity.setName("kafka2");
        registerServiceEntity.setVersion("2.0");
        registerServiceEntity.setCsp(Csp.HUAWEI);
        registerServiceEntity.setCategory(Category.AI);
        registerServiceEntity.setOcl(ocl);
        registerServiceEntity.setServiceState(ServiceState.REGISTERED);
        DeployVariableValidator deployVariableValidator = new DeployVariableValidator();
        OpenApiUtil openApiUtil = new OpenApiUtil(deployVariableValidator);
        String s = openApiUtil.creatServiceApi(registerServiceEntity);
        log.info(s);
        String filePath = "openapi/" + registerServiceEntity.getId() + ".html";
        Path path = Paths.get(filePath);
        Assertions.assertFalse(Files.exists(path));
    }

    @Test
    public void updateServiceApi_tese() throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new URL("file:src/test/resources/ocl_testOpenApi.yaml"));
        RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setId(UUID.randomUUID());
        registerServiceEntity.setName("kafka");
        registerServiceEntity.setVersion("1.0");
        registerServiceEntity.setCsp(Csp.HUAWEI);
        registerServiceEntity.setCategory(Category.AI);
        registerServiceEntity.setOcl(ocl);
        registerServiceEntity.setServiceState(ServiceState.REGISTERED);
        DeployVariableValidator deployVariableValidator = new DeployVariableValidator();
        OpenApiUtil openApiUtil = new OpenApiUtil(deployVariableValidator);
        Assertions.assertDoesNotThrow(() -> openApiUtil.updateServiceApi(registerServiceEntity));
    }

    @Test
    public void deleteServiceApi_tese() {
        String id = "624e2a47-b1be-414d-b8c5-967e09c13dfe";
        DeployVariableValidator deployVariableValidator = new DeployVariableValidator();
        OpenApiUtil openApiUtil = new OpenApiUtil(deployVariableValidator);
        openApiUtil.deleteServiceApi(id);
        String filePath = "openapi/" + id + ".html";
        Path path = Paths.get(filePath);
        Assertions.assertTrue(!Files.exists(path));
    }
}