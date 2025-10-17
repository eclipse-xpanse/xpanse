/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.PathItem;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;

/**
 * Data model to describe the write methods that must be added to the service controllers OpenAPI
 * schema.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApiWriteMethodConfig extends ApiMethodConfig {

    @NotNull
    @Schema(
            description =
                    "type of the service order type related to this method. This is necessary since"
                            + " for some cases such as objects and deployment, multiple order types"
                            + " must be described within one description.")
    private ServiceOrderType serviceOrderType;

    @Schema(description = "type of the http method to be created")
    private PathItem.HttpMethod httpMethod;

    private CustomBody requestBody;

    @Schema(
            description =
                    "if this field is null, the generator will automatically create responses with"
                            + " no body and HTTP 200 or 202 based on the use case. ")
    private CustomBody responseBody;
}
