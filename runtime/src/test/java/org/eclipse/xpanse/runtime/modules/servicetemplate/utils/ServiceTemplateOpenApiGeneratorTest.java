/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.modules.servicetemplate.utils;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.common.openapi.OpenApiGeneratorJarManage;
import org.eclipse.xpanse.common.openapi.OpenApiUrlManage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.service.utils.ServiceDeployVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.servicetemplate.utils.AvailabilityZoneSchemaValidator;
import org.eclipse.xpanse.modules.servicetemplate.utils.DeployVariableSchemaValidator;
import org.eclipse.xpanse.modules.servicetemplate.utils.ServiceTemplateOpenApiGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Test for ServiceTemplateOpenApiGenerator.
 */
@Slf4j
class ServiceTemplateOpenApiGeneratorTest {

    private static final String appVersion = "1.0.0";
    private UUID serviceId;
    private OpenApiGeneratorJarManage openApiGeneratorJarManage;
    @InjectMocks
    private ServiceTemplateOpenApiGenerator openApiGenerator;

    @BeforeEach
    void init() {
        String openApiPath = "openapi/";
        Integer serverPort = 8080;
        serviceId = UUID.randomUUID();
        OpenApiUrlManage openApiUrlManage = new OpenApiUrlManage(openApiPath, serverPort);
        String clientDownloadURL = "https://repo1.maven.org/maven2/org/"
                + "openapitools/openapi-generator-cli/6.6.0/openapi-generator-cli-6.6.0.jar";
        openApiGeneratorJarManage =
                new OpenApiGeneratorJarManage(clientDownloadURL, openApiPath);
        PluginManager pluginManager = new PluginManager();
        openApiGenerator = new ServiceTemplateOpenApiGenerator(appVersion, null,
                pluginManager, openApiUrlManage, openApiGeneratorJarManage);
    }

    void setConfiguration(Boolean webSecurityIsEnabled, Boolean roleProtectionIsEnabled) {

        ReflectionTestUtils.setField(openApiGenerator, "appVersion", appVersion);

        ReflectionTestUtils.setField(openApiGenerator, "webSecurityIsEnabled",
                webSecurityIsEnabled);
        ReflectionTestUtils.setField(openApiGenerator, "roleProtectionIsEnabled",
                roleProtectionIsEnabled);
    }

    @Test
    void openApiGeneratorTestWithOauthIsDisabled() throws Exception {
        setConfiguration(false, false);
        createServiceApi();
        updateServiceApi();
        deleteServiceApi();
    }

    @Test
    void openApiGeneratorTestWithRoleProtectionIsEnabled() throws Exception {
        setConfiguration(true, true);
        createServiceApi();
        updateServiceApi();
        deleteServiceApi();
    }

    @Test
    void openApiGeneratorTestWithRoleProtectionIsDisabled() throws Exception {
        setConfiguration(true, false);
        createServiceApi();
        updateServiceApi();
        deleteServiceApi();
    }


    void createServiceApi() throws Exception {
        ServiceTemplateEntity serviceTemplateEntity = getServiceTemplateEntity(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        openApiGenerator.createServiceApi(serviceTemplateEntity);
        String openApiWorkdir = openApiGeneratorJarManage.getOpenApiWorkdir();
        File htmlFile = new File(openApiWorkdir, serviceTemplateEntity.getId() + ".html");
        Assertions.assertTrue(htmlFile.exists());
    }

    void updateServiceApi() throws Exception {
        ServiceTemplateEntity serviceTemplateEntity = getServiceTemplateEntity(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        Assertions.assertDoesNotThrow(
                () -> openApiGenerator.updateServiceApi(serviceTemplateEntity));
    }

    void deleteServiceApi() {
        openApiGenerator.deleteServiceApi(serviceId.toString());
        String openApiWorkdir = openApiGeneratorJarManage.getOpenApiWorkdir();
        File htmlFile = new File(openApiWorkdir, serviceId + ".html");
        Assertions.assertFalse(htmlFile.exists());
    }

    ServiceTemplateEntity getServiceTemplateEntity(URL url) throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(url);
        AvailabilityZoneSchemaValidator.validateServiceAvailabilities(
                ocl.getDeployment().getServiceAvailabilityConfig());
        DeployVariableSchemaValidator.validateDeployVariable(
                ocl.getDeployment().getVariables());
        ServiceDeployVariablesJsonSchemaGenerator serviceDeployVariablesJsonSchemaGenerator =
                new ServiceDeployVariablesJsonSchemaGenerator();
        JsonObjectSchema jsonObjectSchema =
                serviceDeployVariablesJsonSchemaGenerator.buildDeployVariableJsonSchema(
                        ocl.getDeployment().getVariables());
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(serviceId);
        serviceTemplateEntity.setName(ocl.getName());
        serviceTemplateEntity.setVersion(ocl.getServiceVersion());
        serviceTemplateEntity.setCsp(ocl.getCloudServiceProvider().getName());
        serviceTemplateEntity.setCategory(ocl.getCategory());
        serviceTemplateEntity.setServiceHostingType(ocl.getServiceHostingType());
        serviceTemplateEntity.setOcl(ocl);
        serviceTemplateEntity.setServiceRegistrationState(
                ServiceRegistrationState.APPROVAL_PENDING);
        serviceTemplateEntity.setJsonObjectSchema(jsonObjectSchema);
        return serviceTemplateEntity;
    }
}
