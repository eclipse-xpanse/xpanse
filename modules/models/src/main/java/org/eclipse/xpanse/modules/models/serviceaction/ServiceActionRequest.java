/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceaction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.Data;

/** ServiceAction request model. */
@Data
public class ServiceActionRequest {

    @NotNull
    @Size(min = 1)
    @Schema(description = "The name service action.")
    private String actionName;

    @NotNull
    @Size(min = 1)
    @Schema(
            description = "The service actions parameter to be modified",
            additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
    private Map<String, Object> actionParameters;
}
