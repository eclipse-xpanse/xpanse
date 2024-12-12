/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceconfiguration;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;

/** service configuration change request order details. */
@Data
public class ServiceConfigurationChangeOrderDetails {

    @NotNull
    @Schema(description = "The id of the order.")
    private UUID orderId;

    @NotNull
    @Schema(description = "Order status of service configuration update result.")
    private TaskStatus orderStatus;

    @NotNull
    @Schema(description = "service configuration requested in the change request.")
    private Map<String, Object> configRequest;

    @NotNull
    @Schema(
            description =
                    "Collection of service configuration change requests"
                            + " generated for the specific change order.")
    private List<ServiceConfigurationChangeDetails> changeRequests;
}
