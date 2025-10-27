/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.controller;

import static org.eclipse.xpanse.modules.servicetemplate.controller.Extensions.X_GLOBAL_SERVICE_MAPPINGS;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceControllerConfigurationInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Bean to create service controller's OpenAPI document based on OCL. */
@Slf4j
@Component
public class ServiceControllerApiManage {

    private final DeploymentMethodsManage deploymentMethodsManage;
    private final ServiceStateMethodsManage serviceStateMethodsManage;
    private final ConfigurationMethodsManage configurationMethodsManage;
    private final ActionMethodsManage actionMethodsManage;
    private final ObjectMethodsManage objectMethodsManage;
    private final FlavorMethodsManage flavorMethodsManage;
    private final CommonReadMethodsManage commonReadMethodsManage;

    /** Bean constructor. */
    @Autowired
    public ServiceControllerApiManage(
            DeploymentMethodsManage deploymentMethodsManage,
            ServiceStateMethodsManage serviceStateMethodsManage,
            ConfigurationMethodsManage configurationMethodsManage,
            ActionMethodsManage actionMethodsManage,
            ObjectMethodsManage objectMethodsManage,
            FlavorMethodsManage flavorMethodsManage,
            CommonReadMethodsManage commonReadMethodsManage) {
        this.deploymentMethodsManage = deploymentMethodsManage;
        this.serviceStateMethodsManage = serviceStateMethodsManage;
        this.configurationMethodsManage = configurationMethodsManage;
        this.actionMethodsManage = actionMethodsManage;
        this.objectMethodsManage = objectMethodsManage;
        this.flavorMethodsManage = flavorMethodsManage;
        this.commonReadMethodsManage = commonReadMethodsManage;
    }

    /**
     * Method which takes OCL and uses the information in it to generate the OpenAPI document of the
     * service controller. The generated API document will be parsed and validated. This OpenAPI
     * document is not CSP specific. Corresponding plugins can further update and make changes as
     * necessary.
     */
    public OpenAPI generateServiceControllerOpenApiDoc(Ocl ocl) {
        OpenAPI openApi =
                new OpenAPI()
                        .info(
                                new Info()
                                        .title(ocl.getName())
                                        .version(ocl.getServiceVersion())
                                        .description(ocl.getDescription()))
                        .specVersion(SpecVersion.V31)
                        .openapi("3.1.0");
        Paths paths = new Paths();
        Components components = new Components();
        openApi.setComponents(components);
        if (ocl.getServiceControllerConfig() != null
                && ocl.getServiceControllerConfig().getStandardNameMappings() != null) {
            openApi.setExtensions(
                    Map.of(
                            X_GLOBAL_SERVICE_MAPPINGS,
                            ocl.getServiceControllerConfig().getStandardNameMappings()));
        }
        openApi.paths(paths);
        if (ocl.getDeployment().getControllerApiMethods() != null
                && ocl.getDeployment().getControllerApiMethods().getApiWriteMethodConfigs() != null
                && !ocl.getDeployment()
                        .getControllerApiMethods()
                        .getApiWriteMethodConfigs()
                        .isEmpty()) {
            deploymentMethodsManage.addDeploymentMethods(openApi, ocl, components);
        }
        if (ocl.getResourceStateManage().getIsResourceStateControllable() != null) {
            serviceStateMethodsManage.addServiceStateMethods(openApi, ocl, components);
        }
        if (ocl.getServiceConfigurationManage() != null) {
            configurationMethodsManage.addServiceConfigMethods(openApi, ocl, components);
        }
        if (ocl.getServiceActions() != null) {
            actionMethodsManage.addServiceActionMethods(openApi, ocl, components);
        }
        if (ocl.getServiceObjects() != null) {
            objectMethodsManage.addServiceObjectMethods(openApi, ocl, components);
        }
        if (ocl.getFlavors().getControllerApiMethods() != null
                && ocl.getFlavors().getControllerApiMethods().getApiWriteMethodConfigs() != null) {
            flavorMethodsManage.addFlavorMethods(openApi, ocl, components);
        }
        if (ocl.getServiceControllerConfig() != null
                && ocl.getServiceControllerConfig().getCommonReadMethods() != null) {
            commonReadMethodsManage.addCommonReadMethods(openApi, ocl, components);
        }
        try {
            SwaggerParseResult parsed =
                    new OpenAPIV3Parser().readContents(Yaml.mapper().writeValueAsString(openApi));
            if (parsed.getMessages() != null && !parsed.getMessages().isEmpty()) {
                parsed.getMessages().forEach(log::error);
                throw new ServiceControllerConfigurationInvalidException(parsed.getMessages());
            }
            return parsed.getOpenAPI();
        } catch (JsonProcessingException e) {
            log.error("Error parsing the generated OpenAPI document", e);
            throw new ServiceControllerConfigurationInvalidException(List.of(e.getMessage()));
        }
    }
}
