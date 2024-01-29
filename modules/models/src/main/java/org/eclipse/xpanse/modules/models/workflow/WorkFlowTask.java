/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.workflow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.Data;

/**
 * WorkFlowTask class.
 */
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

    @NotEmpty
    @Schema(description = "The businessKey of the Process")
    private String businessKey;

    @NotNull
    @Schema(description = "The status of the Task")
    private TaskStatus status;

    @NotNull
    @Schema(description = "The create time of the task")
    private Date createTime;
}
