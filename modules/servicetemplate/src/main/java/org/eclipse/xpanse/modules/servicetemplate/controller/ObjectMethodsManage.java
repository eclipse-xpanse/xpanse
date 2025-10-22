/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.controller;

import static org.eclipse.xpanse.modules.servicetemplate.controller.Extensions.X_OBJECT_NAME;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.serviceobject.ServiceObjectDetails;
import org.eclipse.xpanse.modules.models.serviceobject.ServiceObjectRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.ObjectManage;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceObject;
import org.eclipse.xpanse.modules.models.servicetemplate.controller.ApiReadMethodConfig;
import org.eclipse.xpanse.modules.models.servicetemplate.controller.ApiWriteMethodConfig;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ObjectActionType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Beans to create OpenAPI methods for service objects management. */
@Component
public class ObjectMethodsManage {

    private final OperationManage operationManage;
    private final RequestSchemaManage requestSchemaManage;
    private final MethodParametersManage methodParametersManage;
    private final ResponseSchemaManage responseSchemaManage;
    private final SchemaUtils schemaUtils;

    /** constructor method. */
    public ObjectMethodsManage(
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

    /** Method to create OpenAPI methods for service objects management. */
    public void addServiceObjectMethods(OpenAPI openApi, Ocl ocl, Components components) {
        if (!ocl.getServiceObjects().isEmpty()) {
            ocl.getServiceObjects()
                    .forEach(
                            serviceObject -> {
                                serviceObject
                                        .getObjectsManage()
                                        .forEach(
                                                objectManage -> {
                                                    handleServiceObjectWriteMethods(
                                                            objectManage, ocl, openApi, components);
                                                    handleServiceObjectReadMethods(
                                                            serviceObject,
                                                            objectManage,
                                                            ocl,
                                                            openApi,
                                                            components);
                                                });
                            });
        }
    }

    private PathItem.HttpMethod getHttpMethodTypeByObjectActionType(
            ObjectActionType objectActionType) {
        return switch (objectActionType) {
            case CREATE -> PathItem.HttpMethod.POST;
            case UPDATE -> PathItem.HttpMethod.PUT;
            case DELETE -> PathItem.HttpMethod.DELETE;
        };
    }

    private void handleServiceObjectWriteMethods(
            ObjectManage objectManage, Ocl ocl, OpenAPI openApi, Components components) {
        if (objectManage.getControllerApiMethods() != null) {
            objectManage
                    .getControllerApiMethods()
                    .getApiWriteMethodConfigs()
                    .forEach(
                            apiWriteMethodConfig ->
                                    handleWriteMethod(
                                            objectManage,
                                            apiWriteMethodConfig,
                                            ocl,
                                            openApi,
                                            components));
        }
    }

    private void handleWriteMethod(
            ObjectManage objectManage,
            ApiWriteMethodConfig apiWriteMethodConfig,
            Ocl ocl,
            OpenAPI openApi,
            Components components) {
        schemaUtils.addTagIfUnique(apiWriteMethodConfig.getServiceGroupName(), openApi);
        Operation operation = operationManage.buildOperationForWriteMethods(apiWriteMethodConfig);
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
                        apiWriteMethodConfig, ServiceObjectRequest.class, ocl, components));
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
                getHttpMethodTypeByObjectActionType(objectManage.getObjectActionType()),
                operation);
    }

    private void handleServiceObjectReadMethods(
            ServiceObject serviceObject,
            ObjectManage objectManage,
            Ocl ocl,
            OpenAPI openApi,
            Components components) {
        if (objectManage.getControllerApiMethods() != null) {
            objectManage
                    .getControllerApiMethods()
                    .getApiReadMethodConfigs()
                    .forEach(
                            apiReadMethodConfig ->
                                    handleApiReadMethod(
                                            serviceObject,
                                            apiReadMethodConfig,
                                            openApi,
                                            ocl,
                                            components));
        }
    }

    private void handleApiReadMethod(
            ServiceObject serviceObject,
            ApiReadMethodConfig apiReadMethodConfig,
            OpenAPI openApi,
            Ocl ocl,
            Components components) {
        schemaUtils.addTagIfUnique(apiReadMethodConfig.getServiceGroupName(), openApi);
        Operation operation = operationManage.buildOperationForWriteMethods(apiReadMethodConfig);
        // use this later to
        // generate the
        // correct
        // controller.
        operation.addExtension(X_OBJECT_NAME, serviceObject.getType());
        if (apiReadMethodConfig.getMethodParameters() != null) {
            apiReadMethodConfig
                    .getMethodParameters()
                    .forEach(
                            parameterConfig -> {
                                operation.addParametersItem(
                                        methodParametersManage.buildMethodParameter(
                                                parameterConfig));
                            });
        }
        operation.responses(
                responseSchemaManage.buildResponseSchema(
                        apiReadMethodConfig.getResponseBody(),
                        ServiceObjectDetails.class,
                        ocl,
                        components,
                        HttpStatus.OK,
                        apiReadMethodConfig.getIsArray(),
                        apiReadMethodConfig.getStandardNameMappings()));
        schemaUtils.addOperation(
                openApi,
                ocl.getServiceControllerConfig().getBaseUri() + apiReadMethodConfig.getServiceUri(),
                PathItem.HttpMethod.GET,
                operation);
    }
}
