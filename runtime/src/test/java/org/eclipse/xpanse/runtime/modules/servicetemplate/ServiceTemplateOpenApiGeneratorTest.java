/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.modules.servicetemplate;

import java.io.File;
import java.net.URL;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.common.openapi.OpenApiGeneratorJarManage;
import org.eclipse.xpanse.common.openapi.OpenApiUrlManage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.utils.DeployVariableValidator;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.servicetemplate.utils.ServiceTemplateOpenApiGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Test for OpenApiUtilTest.
 */
@Slf4j
@TestMethodOrder(OrderAnnotation.class)
class ServiceTemplateOpenApiGeneratorTest {

    private final String ID = "488adf44-b48f-43fb-9b7f-61e79f40016a";
    private final String CLIENT_DOWNLOAD_URL = "https://repo1.maven.org/maven2/org/"
            + "openapitools/openapi-generator-cli/6.6.0/openapi-generator-cli-6.6.0.jar";
    private final String OPENAPI_PATH = "openapi/";
    private final Integer SERVICER_PORT = 8080;
    private final UUID RANDOM_UUID = UUID.fromString(ID);

    @Test
    @Order(1)
    void createServiceApi_test() throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new URL("file:src/test/resources/ocl_testOpenApi.yaml"));
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(RANDOM_UUID);
        serviceTemplateEntity.setName("kafka2");
        serviceTemplateEntity.setVersion("2.0");
        serviceTemplateEntity.setCsp(Csp.HUAWEI);
        serviceTemplateEntity.setCategory(Category.MIDDLEWARE);
        serviceTemplateEntity.setOcl(ocl);
        serviceTemplateEntity.setServiceRegistrationState(ServiceRegistrationState.REGISTERED);
        DeployVariableValidator deployVariableValidator = new DeployVariableValidator();
        OpenApiUrlManage openApiUrlManage = new OpenApiUrlManage(OPENAPI_PATH, SERVICER_PORT);
        OpenApiGeneratorJarManage openApiGeneratorJarManage = new OpenApiGeneratorJarManage(CLIENT_DOWNLOAD_URL, OPENAPI_PATH);
        ServiceTemplateOpenApiGenerator serviceTemplateOpenApiGenerator =
                new ServiceTemplateOpenApiGenerator(
                        deployVariableValidator, openApiUrlManage, openApiGeneratorJarManage);
        serviceTemplateOpenApiGenerator.createServiceApi(serviceTemplateEntity);
        String openApiWorkdir = openApiGeneratorJarManage.getOpenApiWorkdir();
        File htmlFile = new File(openApiWorkdir, ID + ".html");
        Assertions.assertTrue(htmlFile.exists());
    }

    @Test
    @Order(2)
    void updateServiceApi_test() throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new URL("file:src/test/resources/ocl_testOpenApi.yaml"));
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(RANDOM_UUID);
        serviceTemplateEntity.setName("kafka");
        serviceTemplateEntity.setVersion("1.0");
        serviceTemplateEntity.setCsp(Csp.HUAWEI);
        serviceTemplateEntity.setCategory(Category.AI);
        serviceTemplateEntity.setOcl(ocl);
        serviceTemplateEntity.setServiceRegistrationState(ServiceRegistrationState.UPDATED);
        DeployVariableValidator deployVariableValidator = new DeployVariableValidator();
        OpenApiUrlManage openApiUrlManage = new OpenApiUrlManage(OPENAPI_PATH, SERVICER_PORT);
        OpenApiGeneratorJarManage openApiGeneratorJarManage = new OpenApiGeneratorJarManage(CLIENT_DOWNLOAD_URL, OPENAPI_PATH);
        ServiceTemplateOpenApiGenerator serviceTemplateOpenApiGenerator =
                new ServiceTemplateOpenApiGenerator(
                        deployVariableValidator, openApiUrlManage,
                        openApiGeneratorJarManage);
        Assertions.assertDoesNotThrow(
                () -> serviceTemplateOpenApiGenerator.updateServiceApi(serviceTemplateEntity));
    }

    @Test
    @Order(3)
    void deleteServiceApi_test() {
        DeployVariableValidator deployVariableValidator = new DeployVariableValidator();
        OpenApiUrlManage openApiUrlManage = new OpenApiUrlManage(OPENAPI_PATH, SERVICER_PORT);
        OpenApiGeneratorJarManage openApiGeneratorJarManage = new OpenApiGeneratorJarManage(CLIENT_DOWNLOAD_URL, OPENAPI_PATH);
        ServiceTemplateOpenApiGenerator serviceTemplateOpenApiGenerator =
                new ServiceTemplateOpenApiGenerator(
                        deployVariableValidator, openApiUrlManage,
                        openApiGeneratorJarManage);
        serviceTemplateOpenApiGenerator.deleteServiceApi(ID);
        String openApiWorkdir = openApiGeneratorJarManage.getOpenApiWorkdir();
        File htmlFile = new File(openApiWorkdir, ID + ".html");
        Assertions.assertFalse(htmlFile.exists());
    }
}
