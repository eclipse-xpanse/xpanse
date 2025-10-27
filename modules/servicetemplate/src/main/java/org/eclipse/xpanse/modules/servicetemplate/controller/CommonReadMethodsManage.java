/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.controller;

import static org.eclipse.xpanse.modules.servicetemplate.controller.Extensions.X_READ_DOMAIN;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderDetails;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.CommonReadDomainType;
import org.eclipse.xpanse.modules.models.workflow.WorkFlowTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Bean to manage common read methods in the controller API. */
@Slf4j
@Component
public class CommonReadMethodsManage {

    private final OperationManage operationManage;
    private final MethodParametersManage methodParametersManage;
    private final ResponseSchemaManage responseSchemaManage;
    private final SchemaUtils schemaUtils;

    /** Constructor method. */
    @Autowired
    public CommonReadMethodsManage(
            OperationManage operationManage,
            MethodParametersManage methodParametersManage,
            ResponseSchemaManage responseSchemaManage,
            SchemaUtils schemaUtils) {
        this.operationManage = operationManage;
        this.methodParametersManage = methodParametersManage;
        this.responseSchemaManage = responseSchemaManage;
        this.schemaUtils = schemaUtils;
    }

    /** Beans to add OpenAPI methods for service deployment, and it's lifecycle management. */
    public void addCommonReadMethods(OpenAPI openApi, Ocl ocl, Components components) {
        ocl.getServiceControllerConfig()
                .getCommonReadMethods()
                .forEach(
                        readMethod -> {
                            log.debug("Adding schema for {}", readMethod.getMethodName());
                            schemaUtils.addTagIfUnique(readMethod.getServiceGroupName(), openApi);
                            Operation operation =
                                    operationManage.buildOperationForWriteMethods(readMethod);
                            operation.addExtension(
                                    X_READ_DOMAIN, readMethod.getReadDomainType().toValue());
                            if (readMethod.getMethodParameters() != null) {
                                readMethod
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
                                            readMethod.getResponseBody(),
                                            getStandardResponseTypeByDomainType(
                                                    readMethod.getReadDomainType()),
                                            ocl,
                                            components,
                                            HttpStatus.OK,
                                            readMethod.getIsArray(),
                                            readMethod.getStandardNameMappings()));
                            schemaUtils.addOperation(
                                    openApi,
                                    ocl.getServiceControllerConfig().getBaseUri()
                                            + readMethod.getServiceUri(),
                                    PathItem.HttpMethod.GET,
                                    operation);
                        });
    }

    private Class<?> getStandardResponseTypeByDomainType(
            CommonReadDomainType commonReadDomainType) {
        return switch (commonReadDomainType) {
            case ORDERS -> ServiceOrderDetails.class;
            case SERVICES -> DeployedServiceDetails.class;
            case WORKFLOWS -> WorkFlowTask.class;
        };
    }
}
