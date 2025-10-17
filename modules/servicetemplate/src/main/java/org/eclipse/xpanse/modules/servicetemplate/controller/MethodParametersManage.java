/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.controller;

import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.eclipse.xpanse.modules.models.servicetemplate.methods.MethodParameter;
import org.springframework.stereotype.Component;

/**
 * Bean to convert method parameter configuration in OCL to Parameter object of OpenAPI data model.
 */
@Component
public class MethodParametersManage {

    /**
     * Method to convert method parameter configuration in OCL to Parameter object of OpenAPI data
     * model.
     */
    public Parameter buildMethodParameter(MethodParameter methodParameter) {
        return new Parameter()
                .description(methodParameter.getDescription())
                .in(methodParameter.getIn())
                .name(methodParameter.getName())
                .required(methodParameter.getMandatory())
                .schema(new StringSchema());
    }
}
