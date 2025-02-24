/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import lombok.Data;

/** WorkFlowTask class. */
@Data
public class WorkFlowTask {

    @NotEmpty
    @Schema(description = "The id of the ProcessInstance")
    private String processInstanceId;

    @Schema(description = "The name of the ProcessInstance")
    private String processInstanceName;

    @NotEmpty
    @Schema(description = "The id of the ProcessDefinition")
    private String processDefinitionId;

    @NotEmpty
    @Schema(description = "The name of the ProcessDefinition")
    private String processDefinitionName;

    @NotEmpty
    @Schema(description = "The execution id of the ProcessInstance")
    private String executionId;

    @NotEmpty
    @Schema(description = "The id of the task")
    private String taskId;

    @NotEmpty
    @Schema(description = "The name of the task")
    private String taskName;

    @Schema(description = "The businessKey of the Process")
    private String businessKey;

    @NotNull
    @Schema(description = "The status of the Task")
    private WorkFlowTaskStatus taskStatus;

    @NotNull
    @Schema(description = "The create time of the task")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime createdTime;
}
