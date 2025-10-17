/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.servicetemplate.controller;

import io.swagger.v3.oas.models.Operation;
import java.util.List;
import org.eclipse.xpanse.modules.models.servicetemplate.controller.ApiMethodConfig;
import org.springframework.stereotype.Component;

/** Creates the Operation object of the OpenAPI data model. */
@Component
public class OperationManage {

    /** Converts OCL data to Operation object of the OpenAPI data model. */
    public Operation buildOperationForWriteMethods(ApiMethodConfig apiMethodConfig) {
        return new Operation()
                .description(apiMethodConfig.getMethodDescription())
                .operationId(apiMethodConfig.getMethodName())
                .tags(List.of(apiMethodConfig.getServiceGroupName()));
    }
}
