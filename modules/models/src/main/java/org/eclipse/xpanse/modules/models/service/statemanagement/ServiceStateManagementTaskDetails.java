/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.statemanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceStateManagementTaskType;

/**
 * Define view object for detail of the service state management task.
 */
@Data
public class ServiceStateManagementTaskDetails {

    @NotNull
    @Schema(description = "The id of the service state management task.")
    private UUID taskId;

    @NotNull
    @Schema(description = "The id of the deployed service.")
    private UUID serviceId;

    @NotNull
    @Schema(description = "The type of the service state management task.")
    private ServiceStateManagementTaskType taskType;

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
}
