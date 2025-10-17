/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.controller;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationDetails;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationUpdate;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Beans to create OpenAPI methods for service configuration management. */
@Component
public class ConfigurationMethodsManage {

    private final OperationManage operationManage;
    private final RequestSchemaManage requestSchemaManage;
    private final MethodParametersManage methodParametersManage;
    private final ResponseSchemaManage responseSchemaManage;
    private final SchemaUtils schemaUtils;

    /** constructor method. */
    public ConfigurationMethodsManage(
            OperationManage operationManage,
            RequestSchemaManage requestSchemaManage,
            MethodParametersManage methodParametersManage,
            ResponseSchemaManage responseSchemaManage,
            SchemaUtils schemaUtils) {
        this.operationManage = operationManage;
        this.requestSchemaManage = requestSchemaManage;
        this.methodParametersManage = methodParametersManage;
        this.responseSchemaManage = responseSchemaManage;
        this.schemaUtils = schemaUtils;
    }

    /** Adds OpenAPI methods for service configuration management. */
    public void addServiceConfigMethods(OpenAPI openApi, Ocl ocl, Components components) {
        if (ocl.getServiceConfigurationManage() != null
                && ocl.getServiceConfigurationManage().getControllerApiMethods() != null) {
            if (ocl.getServiceConfigurationManage()
                            .getControllerApiMethods()
                            .getApiWriteMethodConfigs()
                    != null) {
                ocl.getServiceConfigurationManage()
                        .getControllerApiMethods()
                        .getApiWriteMethodConfigs()
                        .forEach(
                                apiWriteMethodConfig -> {
                                    schemaUtils.addTagIfUnique(
                                            apiWriteMethodConfig.getServiceGroupName(), openApi);
                                    Operation operation =
                                            operationManage.buildOperationForWriteMethods(
                                                    apiWriteMethodConfig);
                                    if (apiWriteMethodConfig.getMethodParameters() != null) {
                                        apiWriteMethodConfig
                                                .getMethodParameters()
                                                .forEach(
                                                        parameterConfig -> {
                                                            operation.addParametersItem(
                                                                    methodParametersManage
                                                                            .buildMethodParameter(
                                                                                    parameterConfig));
                                                        });
                                    }

                                    operation.requestBody(
                                            requestSchemaManage.buildRequestBody(
                                                    apiWriteMethodConfig,
                                                    ServiceConfigurationUpdate.class,
                                                    ocl,
                                                    components));
                                    operation.responses(
                                            responseSchemaManage.buildResponseSchema(
                                                    apiWriteMethodConfig.getResponseBody(),
                                                    ServiceOrder.class,
                                                    ocl,
                                                    components,
                                                    HttpStatus.ACCEPTED,
                                                    false,
                                                    null));
                                    schemaUtils.addOperation(
                                            openApi,
                                            ocl.getServiceControllerConfig().getBaseUri()
                                                    + apiWriteMethodConfig.getServiceUri(),
                                            PathItem.HttpMethod.PUT,
                                            operation);
                                });
            }
            if (ocl.getServiceConfigurationManage()
                            .getControllerApiMethods()
                            .getApiReadMethodConfigs()
                    != null) {
                ocl.getServiceConfigurationManage()
                        .getControllerApiMethods()
                        .getApiReadMethodConfigs()
                        .forEach(
                                apiReadMethodConfig -> {
                                    schemaUtils.addTagIfUnique(
                                            apiReadMethodConfig.getServiceGroupName(), openApi);
                                    Operation operation =
                                            operationManage.buildOperationForWriteMethods(
                                                    apiReadMethodConfig);
                                    if (apiReadMethodConfig.getMethodParameters() != null) {
                                        apiReadMethodConfig
                                                .getMethodParameters()
                                                .forEach(
                                                        parameterConfig -> {
                                                            operation.addParametersItem(
                                                                    methodParametersManage
                                                                            .buildMethodParameter(
                                                                                    parameterConfig));
                                                        });
                                    }
                                    operation.responses(
                                            responseSchemaManage.buildResponseSchema(
                                                    apiReadMethodConfig.getResponseBody(),
                                                    ServiceConfigurationDetails.class,
                                                    ocl,
                                                    components,
                                                    HttpStatus.OK,
                                                    apiReadMethodConfig.getIsArray(),
                                                    apiReadMethodConfig.getStandardNameMappings()));
                                    schemaUtils.addOperation(
                                            openApi,
                                            ocl.getServiceControllerConfig().getBaseUri()
                                                    + apiReadMethodConfig.getServiceUri(),
                                            PathItem.HttpMethod.GET,
                                            operation);
                                });
            }
        }
    }
}
