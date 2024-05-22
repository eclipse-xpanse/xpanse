/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.modify;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;

/**
 * Define view object for detail of the service modification audit.
 */
@Data
public class ServiceModificationAuditDetails {

    @NotNull
    @Schema(description = "The id of the service modification request.")
    private UUID id;

    @NotNull
    @Schema(description = "The id of the deployed service.")
    private UUID serviceId;

    @NotNull
    @Schema(description = "The status of the service state management task.")
    private TaskStatus taskStatus;

    @Schema(description = "The error message of the failed management task.")
    private String errorMsg;

    @Schema(description = "The started time of the task.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime startedTime;

    @Schema(description = "The completed time of the task.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime completedTime;

    @NotNull
    @Schema(description = "The deploy request of the service before this modification.")
    private DeployRequest previousDeployRequest;

    @NotNull
    @Schema(description = "The deploy request of the service after this modification.")
    private DeployRequest newDeployRequest;

    @Schema(description = "The deployed resource list of the service before this modification.")
    private List<DeployResource> previousDeployedResources;

    @Schema(description = "The properties of the deployed service before this modification.")
    private Map<String, String> previousDeployedServiceProperties;

    @Schema(description = "The properties of the deployed result before this modification.")
    private Map<String, String> previousDeployedResultProperties;
}
