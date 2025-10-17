/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.controller;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.controller.ApiWriteMethodConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Beans to create OpenAPI methods for service deployment, and it's lifecycle management. */
@Component
public class DeploymentMethodsManage {

    private final OperationManage operationManage;
    private final RequestSchemaManage requestSchemaManage;
    private final MethodParametersManage methodParametersManage;
    private final ResponseSchemaManage responseSchemaManage;
    private final SchemaUtils schemaUtils;

    /** Constructor method. */
    @Autowired
    public DeploymentMethodsManage(
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

    /** Beans to add OpenAPI methods for service deployment, and it's lifecycle management. */
    public void addDeploymentMethods(OpenAPI openApi, Ocl ocl, Components components) {
        ocl.getDeployment()
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
                                            getRequestType(
                                                    apiWriteMethodConfig.getServiceOrderType()),
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
                                    getHttpMethodType(apiWriteMethodConfig),
                                    operation);
                        });
    }

    private Class<?> getRequestType(ServiceOrderType serviceOrderType) {
        return switch (serviceOrderType) {
            case DEPLOY, MODIFY -> DeployRequest.class;
            default -> null;
        };
    }

    private PathItem.HttpMethod getHttpMethodTypeByServiceOrderType(
            ServiceOrderType serviceOrderType) {
        return switch (serviceOrderType) {
            case DEPLOY -> PathItem.HttpMethod.POST;
            case MODIFY, PORT, RETRY, RECREATE -> PathItem.HttpMethod.PUT;
            case DESTROY -> PathItem.HttpMethod.DELETE;
            default -> null;
        };
    }

    private PathItem.HttpMethod getHttpMethodType(ApiWriteMethodConfig apiWriteMethodConfig) {
        return apiWriteMethodConfig.getHttpMethod() != null
                ? apiWriteMethodConfig.getHttpMethod()
                : getHttpMethodTypeByServiceOrderType(apiWriteMethodConfig.getServiceOrderType());
    }
}
