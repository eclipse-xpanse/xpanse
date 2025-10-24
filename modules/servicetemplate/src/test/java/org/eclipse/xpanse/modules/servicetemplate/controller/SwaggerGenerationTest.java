/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.controller;

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URI;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.service.utils.ServiceInputVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
            ServiceControllerApiManage.class,
            DeploymentMethodsManage.class,
            MethodParametersManage.class,
            OperationManage.class,
            RequestSchemaManage.class,
            ResponseSchemaManage.class,
            SchemaUtils.class,
            OclLoader.class,
            ServiceStateMethodsManage.class,
            ConfigurationMethodsManage.class,
            ServiceConfigurationVariablesJsonSchemaGenerator.class,
            ServiceInputVariablesJsonSchemaGenerator.class,
            ActionMethodsManage.class,
            ObjectMethodsManage.class,
            FlavorMethodsManage.class,
            CommonReadMethodsManage.class,
        })
public class SwaggerGenerationTest {

    @Autowired private OclLoader oclLoader;

    @Inject ServiceControllerApiManage serviceControllerApiManage;

    @Test
    void testSwaggerGeneration() throws IOException {
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_test_swagger_generate.yml")
                                .toURL());
        OpenAPI openAPI = serviceControllerApiManage.generateServiceControllerOpenApiDoc(ocl);
        Yaml.mapper().writeValueAsString(openAPI);
        Assertions.assertNotNull(openAPI);
    }
}
