/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicechange;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;

/** service change request order details. */
@Data
public class ServiceChangeOrderDetails {

    @NotNull
    @Schema(description = "The id of the order.")
    private UUID orderId;

    @NotNull
    @Schema(description = "Order status of service update request.")
    private TaskStatus orderStatus;

    @NotNull
    @Schema(description = "service change request properties.")
    private Map<String, Object> serviceChangeRequest;

    @NotNull
    @Schema(
            description =
                    "Collection of service change details requests"
                            + " generated for the specific change order.")
    private List<ServiceChangeDetails> changeRequests;
}
