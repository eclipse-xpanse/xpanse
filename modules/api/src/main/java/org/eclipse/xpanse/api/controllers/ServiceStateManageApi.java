/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.deployment.ServiceStateManager;
import org.eclipse.xpanse.modules.models.service.statemanagement.ServiceStateManagementTaskDetails;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ManagementTaskStatus;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceStateManagementTaskType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service Management REST API.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
public class ServiceStateManageApi {

    @Resource
    private ServiceStateManager serviceStateManager;

    /**
     * Start the deployed service by the service id.
     *
     * @param serviceId id of service.
     * @return id of the service state management task.
     */
    @Tag(name = "ServiceStatusManagement",
            description = "APIs to manage status of the service instances")
    @Operation(description = "Start a task to start the service instance.")
    @PutMapping(value = "/services/start/{serviceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public UUID startService(@PathVariable("serviceId") String serviceId) {
        return serviceStateManager.startService(UUID.fromString(serviceId));
    }

    /**
     * Stop the deployed service by the service id.
     *
     * @param serviceId id of service.
     * @return id of the service state management task.
     */
    @Tag(name = "ServiceStatusManagement",
            description = "APIs to manage status of the service instances")
    @Operation(description = "Start a task to stop the service instance.")
    @PutMapping(value = "/services/stop/{serviceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public UUID stopService(@PathVariable("serviceId") String serviceId) {
        return serviceStateManager.stopService(UUID.fromString(serviceId));
    }

    /**
     * Restart the deployed service by the service id.
     *
     * @param serviceId id of service.
     * @return id of the service state management task.
     */
    @Tag(name = "ServiceStatusManagement",
            description = "APIs to manage status of the service instances")
    @Operation(description = "Start a task to restart the service instance.")
    @PutMapping(value = "/services/restart/{serviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public UUID restartService(@PathVariable("serviceId") String serviceId) {
        return serviceStateManager.restartService(UUID.fromString(serviceId));
    }


    /**
     * List service state management tasks.
     *
     * @param serviceId  id of service.
     * @param taskType   type of the management task.
     * @param taskStatus status of the management task.
     * @return service state management tasks.
     */
    @Tag(name = "ServiceStatusManagement",
            description = "APIs to manage status of the service instances")
    @Operation(description = "List state management tasks of the service.")
    @GetMapping(value = "/services/{serviceId}/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public List<ServiceStateManagementTaskDetails> listServiceStateManagementTasks(
            @Parameter(name = "serviceId", description = "id of the service")
            @PathVariable(name = "serviceId") String serviceId,
            @Parameter(name = "taskType", description = "type of the management task")
            @RequestParam(name = "taskType", required = false)
            ServiceStateManagementTaskType taskType,
            @Parameter(name = "taskStatus", description = "status of the management task")
            @RequestParam(name = "taskStatus", required = false) ManagementTaskStatus taskStatus) {
        return serviceStateManager.listServiceStateManagementTasks(UUID.fromString(serviceId),
                taskType, taskStatus);
    }


    /**
     * Delete all state management tasks of the service.
     *
     * @param serviceId id of the service.
     */
    @Tag(name = "ServiceStatusManagement",
            description = "APIs to manage status of the service instances")
    @Operation(description = "Delete all state management tasks of the service.")
    @DeleteMapping(value = "/services/{serviceId}/tasks",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public void deleteManagementTasksByServiceId(
            @Parameter(name = "serviceId", description = "id of the service")
            @PathVariable(name = "serviceId") String serviceId) {
        serviceStateManager.deleteManagementTasksByServiceId(UUID.fromString(serviceId));
    }


    /**
     * Get state management task details by the task id.
     *
     * @param taskId id of the task.
     * @return state management task details.
     */
    @Tag(name = "ServiceStatusManagement",
            description = "APIs to manage status of the service instances")
    @Operation(description = "Get state management task details by the task id.")
    @GetMapping(value = "/services/tasks/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromManagementTaskId")
    public ServiceStateManagementTaskDetails getManagementTaskDetailsByTaskId(
            @Parameter(name = "taskId", description = "id of the task")
            @PathVariable(name = "taskId") String taskId) {
        return serviceStateManager.getManagementTaskDetailsByTaskId(UUID.fromString(taskId));
    }

    /**
     * Delete service state management task by the task id.
     *
     * @param taskId id of the service.
     */
    @Tag(name = "ServiceStatusManagement",
            description = "APIs to manage status of the service instances")
    @Operation(description = "Delete service state management task by the task id.")
    @DeleteMapping(value = "/services/tasks/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditApiRequest(methodName = "getCspFromManagementTaskId")
    public void deleteManagementTaskByTaskId(
            @Parameter(name = "taskId", description = "id of the task")
            @PathVariable(name = "taskId") String taskId) {
        serviceStateManager.deleteManagementTaskByTaskId(UUID.fromString(taskId));
    }
}