/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deployment;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Data;

/** ModifyRequest model. */
@Data
public class ModifyRequest {

    /** Customer provided name for the service. */
    @Schema(
            description =
                    "Customer's name for the service. Used only for customer's reference. If not "
                            + "provided,"
                            + " the existing customerServiceName from the service will be reused.")
    private String customerServiceName;

    /** The flavor of the Service. */
    @Schema(description = "The flavor of the Service.")
    private String flavor;

    /** The property of the Service. */
    @Schema(
            description = "The properties for the requested service",
            additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
    private Map<String, Object> serviceRequestProperties;
}
