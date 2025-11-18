/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.deployment.serviceporting.consts.ServicePortingConstants;
import org.eclipse.xpanse.modules.models.workflow.WorkFlowTask;
import org.eclipse.xpanse.modules.models.workflow.WorkFlowTaskStatus;
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
import org.eclipse.xpanse.modules.workflow.utils.WorkflowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** REST API methods for workflow. */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
@ConditionalOnProperty(
        name = "xpanse.agent-api.enable-agent-api-only",
        havingValue = "false",
        matchIfMissing = true)
public class WorkFlowApi {

    private final WorkflowUtils workflowUtils;
    private final UserServiceHelper userServiceHelper;

    /** Constructor method. */
    @Autowired
    public WorkFlowApi(WorkflowUtils workflowUtils, UserServiceHelper userServiceHelper) {
        this.workflowUtils = workflowUtils;
        this.userServiceHelper = userServiceHelper;
    }

    /** Query tasks of the given user by status. */
    @Tag(name = "Workflow", description = "APIs to manage the Workflow")
    @Operation(description = "Query all tasks of the given user")
    @GetMapping(value = "/workflow/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(enabled = false)
    public List<WorkFlowTask> queryTasks(
            @Parameter(name = "status", description = "the status of task")
                    @RequestParam(name = "status", required = false)
                    WorkFlowTaskStatus status) {
        String currentUserId = userServiceHelper.getCurrentUserId();
        return workflowUtils.queryAllTasks(status, currentUserId);
    }

    /** Complete tasks based on task ID and set global process variables. */
    @Tag(name = "Workflow", description = "APIs to manage the Workflow")
    @Operation(description = "Complete tasks by task ID and set global process variables .")
    @PutMapping(
            value = "/workflow/complete/task/{taskId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromWorkflowTaskId")
    public void completeTask(
            @Parameter(
                            name = "taskId",
                            description = "ID of the workflow task that needs to be handled")
                    @PathVariable("taskId")
                    String taskId,
            @RequestBody Map<String, Object> variables) {
        workflowUtils.completeTask(taskId, variables);
    }

    /** Manage failed workflow tasks. */
    @Tag(name = "Workflow", description = "APIs to manage the Workflow")
    @Operation(description = "Manage failed task orders.")
    @PutMapping(value = "/workflow/task/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromWorkflowTaskId")
    public void manageFailedOrder(
            @Parameter(
                            name = "taskId",
                            description = "ID of the workflow task that needs to be handled")
                    @PathVariable("taskId")
                    String taskId,
            @Parameter(
                            name = "retryOrder",
                            description =
                                    "Controls if the order must be retried again or simply closed.")
                    @RequestParam(name = "retryOrder")
                    boolean retryOrder) {
        Map<String, Object> variables = new HashMap<>();
        variables.put(ServicePortingConstants.IS_RETRY_TASK, retryOrder);
        workflowUtils.completeTask(taskId, variables);
    }
}
