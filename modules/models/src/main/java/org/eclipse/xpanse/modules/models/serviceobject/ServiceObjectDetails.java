/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceobject;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderDetails;

/** Details model of Service Object. */
@Data
public class ServiceObjectDetails {

    @NotNull
    @Schema(description = "The id of the service object.")
    private UUID objectId;

    @NotNull
    @Schema(description = "The id of the service deployment.")
    private UUID serviceId;

    @NotNull
    @NotBlank
    @Schema(description = "The type of service object.")
    private String objectType;

    @NotNull
    @NotBlank
    @Schema(description = "The name of service object identifier.")
    private String objectIdentifierName;

    @Schema(description = "The collection of dependent object IDs.")
    private Set<UUID> dependentObjectIds;

    @NotNull
    @Schema(description = "The collection of service object parameter.")
    private Map<String, Object> parameters;

    @NotNull
    @Schema(description = "The collection of ids of the service orders result in the object.")
    private List<ServiceOrderDetails> objectOrderHistory;
}
