/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.register;

import java.io.File;
import java.net.URL;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.common.openapi.OpenApiUtil;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.eclipse.xpanse.modules.models.service.register.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.service.utils.DeployVariableValidator;
import org.eclipse.xpanse.modules.models.service.utils.OclLoader;
import org.eclipse.xpanse.modules.register.register.utils.RegisteredServicesOpenApiGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for OpenApiUtilTest.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
class OpenApiUtilTest {

    private static final String ID = "488adf44-b48f-43fb-9b7f-61e79f40016a";
    private static final String CLIENT_DOWNLOAD_URL = "https://repo1.maven.org/maven2/org/"
            + "openapitools/openapi-generator-cli/6.5.0/openapi-generator-cli-6.5.0.jar";
    private static final String OPENAPI_PATH = "openapi/";
    private static final Integer SERVICER_PORT = 8080;
    private static UUID RANDOM_UUID;

    @BeforeAll
    static void init() {
        RANDOM_UUID = UUID.fromString(ID);
    }

    @Test
    @Order(1)
    void createServiceApi_test() throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new URL("file:src/test/resources/ocl_testOpenApi.yaml"));
        RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setId(RANDOM_UUID);
        registerServiceEntity.setName("kafka2");
        registerServiceEntity.setVersion("2.0");
        registerServiceEntity.setCsp(Csp.HUAWEI);
        registerServiceEntity.setCategory(Category.MIDDLEWARE);
        registerServiceEntity.setOcl(ocl);
        registerServiceEntity.setServiceRegistrationState(ServiceRegistrationState.REGISTERED);
        DeployVariableValidator deployVariableValidator = new DeployVariableValidator();
        OpenApiUtil openApiUtil = new OpenApiUtil(CLIENT_DOWNLOAD_URL, OPENAPI_PATH, SERVICER_PORT);
        RegisteredServicesOpenApiGenerator registeredServicesOpenApiGenerator = new RegisteredServicesOpenApiGenerator(
                deployVariableValidator, openApiUtil);
        registeredServicesOpenApiGenerator.createServiceApi(registerServiceEntity);
        String openApiWorkdir = openApiUtil.getOpenApiWorkdir();
        File htmlFile = new File(openApiWorkdir, ID + ".html");
        Assertions.assertTrue(htmlFile.exists());
    }

    @Test
    @Order(2)
    void updateServiceApi_test() throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new URL("file:src/test/resources/ocl_testOpenApi.yaml"));
        RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setId(RANDOM_UUID);
        registerServiceEntity.setName("kafka");
        registerServiceEntity.setVersion("1.0");
        registerServiceEntity.setCsp(Csp.HUAWEI);
        registerServiceEntity.setCategory(Category.AI);
        registerServiceEntity.setOcl(ocl);
        registerServiceEntity.setServiceRegistrationState(ServiceRegistrationState.UPDATED);
        DeployVariableValidator deployVariableValidator = new DeployVariableValidator();
        OpenApiUtil openApiUtil = new OpenApiUtil(CLIENT_DOWNLOAD_URL,
                OPENAPI_PATH, SERVICER_PORT);
        RegisteredServicesOpenApiGenerator registeredServicesOpenApiGenerator = new RegisteredServicesOpenApiGenerator(
                deployVariableValidator, openApiUtil);
        Assertions.assertDoesNotThrow(() -> registeredServicesOpenApiGenerator.updateServiceApi(registerServiceEntity));
    }

    @Test
    @Order(3)
    void deleteServiceApi_test() {
        DeployVariableValidator deployVariableValidator = new DeployVariableValidator();
        OpenApiUtil openApiUtil = new OpenApiUtil(CLIENT_DOWNLOAD_URL,
                OPENAPI_PATH, SERVICER_PORT);
        RegisteredServicesOpenApiGenerator registeredServicesOpenApiGenerator = new RegisteredServicesOpenApiGenerator(
                deployVariableValidator, openApiUtil);
        registeredServicesOpenApiGenerator.deleteServiceApi(ID);
        String openApiWorkdir = openApiUtil.getOpenApiWorkdir();
        File htmlFile = new File(openApiWorkdir, ID + ".html");
        Assertions.assertFalse(htmlFile.exists());
    }
}
