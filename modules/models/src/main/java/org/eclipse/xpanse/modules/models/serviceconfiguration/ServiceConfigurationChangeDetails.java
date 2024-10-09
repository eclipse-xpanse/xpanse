/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.serviceconfiguration;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.serviceconfiguration.enums.ServiceConfigurationStatus;

/**
 * service configuration change request details.
 */
@Data
public class ServiceConfigurationChangeDetails {

    @NotNull
    @Schema(description = "ID of the change request created as part of the change order.")
    private UUID changeId;

    @Schema(description = "name of the resource on which the change request is executed. "
            + "Null means any one of the resources that is part of the service "
            + "and is of type configManager can execute it and until now none of "
            + "the resource have picked up this request.")
    private String resourceName;

    @NotNull
    @Schema(description = "type of the resource in the service "
            + "that must execute the change request.")
    private String configManager;

    @Schema(description = "message of change service configuration update request.")
    private String resultMessage;

    @NotNull
    @Schema(description = "service configuration requested in the change request.")
    private Map<String, Object> properties;

    @NotNull
    @Schema(description = "status of change service configuration update request.")
    private ServiceConfigurationStatus status;
}
