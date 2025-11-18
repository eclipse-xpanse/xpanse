/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime.modules.servicetemplate.utils;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.common.config.OpenApiGeneratorProperties;
import org.eclipse.xpanse.common.openapi.OpenApiGeneratorJarManage;
import org.eclipse.xpanse.common.openapi.OpenApiUrlManage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.service.utils.ServiceInputVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.servicetemplate.InputVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.DeploymentVariableHelper;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.security.config.SecurityProperties;
import org.eclipse.xpanse.modules.servicetemplate.utils.AvailabilityZoneSchemaValidator;
import org.eclipse.xpanse.modules.servicetemplate.utils.InputVariablesSchemaValidator;
import org.eclipse.xpanse.modules.servicetemplate.utils.ServiceTemplateOpenApiGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** Test for ServiceTemplateOpenApiGenerator. */
@Slf4j
@ContextConfiguration(
        classes = {
            ServiceTemplateOpenApiGenerator.class,
            SecurityProperties.class,
            OpenApiGeneratorProperties.class,
            OpenApiUrlManage.class,
            OpenApiGeneratorJarManage.class
        })
@TestPropertySource(
        properties = {
            "server.port=8080",
            "xpanse.security.enable-web-security=true",
            "xpanse.openapi-generator.file-resources-uri=/openapi/*",
            "xpanse.openapi-generator.file-generation-path=openapi/",
            "xpanse.openapi-generator.client.version=6.6.0",
            "xpanse.openapi-generator.client.download-url=:https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/6.6.0/openapi-generator-cli-6.6.0.jar"
        })
@Import(RefreshAutoConfiguration.class)
@ExtendWith(SpringExtension.class)
class ServiceTemplateOpenApiGeneratorTest {

    private static final String appVersion = "1.0.0";
    private UUID serviceId = UUID.randomUUID();
    @Autowired private OpenApiGeneratorJarManage openApiGeneratorJarManage;
    @Autowired private ServiceTemplateOpenApiGenerator openApiGenerator;
    @MockitoBean private PluginManager pluginManager;
    @MockitoBean private Environment environment;
    @Autowired SecurityProperties securityProperties;

    void setConfiguration(Boolean webSecurityIsEnabled, Boolean roleProtectionIsEnabled) {
        ReflectionTestUtils.setField(securityProperties, "enableWebSecurity", webSecurityIsEnabled);
        ReflectionTestUtils.setField(
                securityProperties, "enableRoleProtection", roleProtectionIsEnabled);
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
        ServiceTemplateEntity serviceTemplateEntity =
                getServiceTemplateEntity(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        openApiGenerator.createServiceApi(serviceTemplateEntity);
        String openApiWorkdir = openApiGeneratorJarManage.getOpenApiWorkdir();
        File htmlFile = new File(openApiWorkdir, serviceTemplateEntity.getId() + ".html");
        Assertions.assertTrue(htmlFile.exists());
    }

    void updateServiceApi() throws Exception {
        ServiceTemplateEntity serviceTemplateEntity =
                getServiceTemplateEntity(
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
        List<InputVariable> inputVariables =
                DeploymentVariableHelper.getInputVariables(ocl.getDeployment());
        InputVariablesSchemaValidator.validateInputVariables(inputVariables);
        ServiceInputVariablesJsonSchemaGenerator serviceInputVariablesJsonSchemaGenerator =
                new ServiceInputVariablesJsonSchemaGenerator();
        JsonObjectSchema jsonObjectSchema =
                serviceInputVariablesJsonSchemaGenerator.buildJsonSchemaOfInputVariables(
                        inputVariables);
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(serviceId);
        serviceTemplateEntity.setName(ocl.getName());
        serviceTemplateEntity.setVersion(ocl.getServiceVersion());
        serviceTemplateEntity.setCsp(ocl.getCloudServiceProvider().getName());
        serviceTemplateEntity.setCategory(ocl.getCategory());
        serviceTemplateEntity.setServiceHostingType(ocl.getServiceHostingType());
        serviceTemplateEntity.setOcl(ocl);
        serviceTemplateEntity.setServiceTemplateRegistrationState(
                ServiceTemplateRegistrationState.APPROVED);
        serviceTemplateEntity.setServiceProviderContactDetails(
                ocl.getServiceProviderContactDetails());
        serviceTemplateEntity.setIsAvailableInCatalog(true);
        serviceTemplateEntity.setIsReviewInProgress(false);
        serviceTemplateEntity.setJsonObjectSchema(jsonObjectSchema);
        return serviceTemplateEntity;
    }
}
