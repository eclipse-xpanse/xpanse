/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceconfiguration;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Data;

/**
 * service configuration change request order details.
 */
@Data
public class ServiceConfigurationUpdateRequestOrderDetails {

    @NotNull
    @Schema(description = "The id of the order.")
    private UUID orderId;

    @NotNull
    @Schema(description = "Collection of service configuration change requests"
            + " generated for the specific change order.")
    private List<ServiceConfigurationChangeRequestDetails> changeRequests;

}
