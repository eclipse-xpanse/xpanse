/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.deployment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import lombok.Data;

/**
 * Data type for a simple deployment request which takes default values for all optional parameters.
 */
@Data
public class SimpleDeployRequest {

    /** Customer provided name for the service. */
    @Schema(description = "ID of the service template that needs to be deployed.")
    @NotNull
    private UUID serviceTemplateId;

    /** Customer provided name for the service. */
    @Schema(
            description =
                    "Customer's name for the service. Used only for customer's reference."
                            + "If not provided, this value will be auto-generated")
    private String customerServiceName;

    /** The property of the Service. */
    @Schema(
            description = "The properties for the requested service",
            additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
    private Map<String, Object> serviceRequestProperties;
}
