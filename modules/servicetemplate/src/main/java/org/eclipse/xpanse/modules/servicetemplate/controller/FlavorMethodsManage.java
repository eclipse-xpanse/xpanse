/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.controller;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.eclipse.xpanse.modules.models.service.deployment.ModifyRequest;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Beans to create OpenAPI methods for service flavor change. */
@Component
public class FlavorMethodsManage {

    private final OperationManage operationManage;
    private final RequestSchemaManage requestSchemaManage;
    private final MethodParametersManage methodParametersManage;
    private final ResponseSchemaManage responseSchemaManage;
    private final SchemaUtils schemaUtils;

    /** Constructor method. */
    public FlavorMethodsManage(
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

    /** Method to create OpenAPI methods for service flavor change. */
    public void addFlavorMethods(OpenAPI openApi, Ocl ocl, Components components) {
        ocl.getFlavors()
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
                                            ModifyRequest.class,
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
}
