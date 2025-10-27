/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.controller;

import static org.eclipse.xpanse.modules.servicetemplate.controller.Extensions.X_ACTION_NAME;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.serviceaction.ServiceActionRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceAction;
import org.eclipse.xpanse.modules.models.servicetemplate.controller.ApiWriteMethodConfig;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Beans to create OpenAPI methods for service actions. */
@Component
public class ActionMethodsManage {

    private final OperationManage operationManage;
    private final RequestSchemaManage requestSchemaManage;
    private final MethodParametersManage methodParametersManage;
    private final ResponseSchemaManage responseSchemaManage;
    private final SchemaUtils schemaUtils;

    /** Constructor method. */
    public ActionMethodsManage(
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

    /** Adds OpenAPI methods for service actions. */
    public void addServiceActionMethods(OpenAPI openApi, Ocl ocl, Components components) {
        if (!ocl.getServiceActions().isEmpty()) {
            ocl.getServiceActions()
                    .forEach(
                            serviceAction ->
                                    handleServiceActionMethods(
                                            serviceAction, ocl, openApi, components));
        }
    }

    private void handleServiceActionMethods(
            ServiceAction serviceAction, Ocl ocl, OpenAPI openApi, Components components) {
        serviceAction
                .getControllerApiMethods()
                .getApiWriteMethodConfigs()
                .forEach(
                        apiWriteMethodConfig ->
                                handleApiWriteMethod(
                                        serviceAction,
                                        apiWriteMethodConfig,
                                        ocl,
                                        openApi,
                                        components));
    }

    private void handleApiWriteMethod(
            ServiceAction serviceAction,
            ApiWriteMethodConfig apiWriteMethodConfig,
            Ocl ocl,
            OpenAPI openApi,
            Components components) {
        schemaUtils.addTagIfUnique(apiWriteMethodConfig.getServiceGroupName(), openApi);
        Operation operation = operationManage.buildOperationForWriteMethods(apiWriteMethodConfig);
        operation.addExtension(X_ACTION_NAME, serviceAction.getName());
        if (apiWriteMethodConfig.getMethodParameters() != null) {
            apiWriteMethodConfig
                    .getMethodParameters()
                    .forEach(
                            parameterConfig -> {
                                operation.addParametersItem(
                                        methodParametersManage.buildMethodParameter(
                                                parameterConfig));
                            });
        }

        operation.requestBody(
                requestSchemaManage.buildRequestBody(
                        apiWriteMethodConfig, ServiceActionRequest.class, ocl, components));
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
    }
}
