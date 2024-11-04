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
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.deployment.ServiceDeploymentEntityHandler;
import org.eclipse.xpanse.modules.deployment.recreate.RecreateService;
import org.eclipse.xpanse.modules.deployment.recreate.consts.RecreateConstants;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceLockedException;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.workflow.recreate.enums.RecreateStatus;
import org.eclipse.xpanse.modules.models.workflow.recreate.view.ServiceRecreateDetails;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.eclipse.xpanse.modules.workflow.utils.WorkflowUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST interface methods for Service Recreate.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
@ConditionalOnProperty(name = "enable.agent.api.only", havingValue = "false", matchIfMissing = true)
public class ServiceRecreateApi {

    @Resource
    private ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;

    @Resource
    private UserServiceHelper userServiceHelper;

    @Resource
    private WorkflowUtils workflowUtils;

    @Resource
    private RecreateService recreateService;

    /**
     * Create a job to recreate the deployed service.
     *
     * @return response
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description = "Create a job to recreate the deployed service.")
    @PutMapping(value = "/services/recreate/{serviceId}", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public UUID recreateService(@Valid @PathVariable("serviceId") String serviceId) {
        ServiceDeploymentEntity serviceDeploymentEntity = serviceDeploymentEntityHandler
                .getServiceDeploymentEntity(UUID.fromString(serviceId));
        String userId = getUserId();
        if (!StringUtils.equals(userId, serviceDeploymentEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to recreate services belonging to other users.");
        }

        if (Objects.nonNull(serviceDeploymentEntity.getLockConfig())
                && serviceDeploymentEntity.getLockConfig().isModifyLocked()) {
            String errorMsg = String.format("Service with id %s is locked from recreate.",
                    UUID.fromString(serviceId));
            throw new ServiceLockedException(errorMsg);
        }

        if (!serviceDeploymentEntity.getServiceDeploymentState()
                .equals(ServiceDeploymentState.DEPLOY_SUCCESS)
                && !serviceDeploymentEntity.getServiceDeploymentState()
                .equals(ServiceDeploymentState.MODIFICATION_FAILED)
                && !serviceDeploymentEntity.getServiceDeploymentState()
                .equals(ServiceDeploymentState.MODIFICATION_SUCCESSFUL)) {
            throw new InvalidServiceStateException(
                    String.format("Service %s with the state %s is not allowed to recreate.",
                            serviceDeploymentEntity.getId(),
                            serviceDeploymentEntity.getServiceDeploymentState()));
        }

        ServiceDeploymentEntity deployedService = serviceDeploymentEntityHandler
                .getServiceDeploymentEntity(UUID.fromString(serviceId));

        Map<String, Object> variable =
                getRecreateProcessVariable(deployedService, userId);
        ProcessInstance instance =
                workflowUtils.startProcess(RecreateConstants.PROCESS_KEY, variable);
        return UUID.fromString(instance.getProcessInstanceId());
    }


    /**
     * List all services recreate by a user.
     *
     * @param recreateId     ID of the service recreate.
     * @param serviceId      ID of the service.
     * @param recreateStatus Status of the service recreate.
     * @return list of all services deployed by a user.
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description = "List all services recreate by a user.")
    @GetMapping(value = "/services/recreate", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceRecreateId")
    public List<ServiceRecreateDetails> listServiceRecreates(
            @Parameter(name = "recreateId", description = "Id of the service recreate")
            @RequestParam(name = "recreateId", required = false) String recreateId,
            @Parameter(name = "serviceId", description = "Id of the old service")
            @RequestParam(name = "serviceId", required = false) String serviceId,
            @Parameter(name = "recreateStatus", description = "Status of the service recreate")
            @RequestParam(name = "recreateStatus", required = false)
            RecreateStatus recreateStatus
    ) {
        return recreateService.listServiceRecreates(UUID.fromString(recreateId),
                UUID.fromString(serviceId),
                recreateStatus, getUserId());
    }

    /**
     * Get recreate records based on recreate id.
     *
     * @param recreateId ID of the service recreate.
     * @return serviceRecreateEntity.
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description = "Get recreate records based on recreate id.")
    @GetMapping(value = "/services/recreate/{recreateId}", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceRecreateId")
    public ServiceRecreateDetails getRecreateOrderDetailsById(
            @Parameter(name = "recreateId", description = "Recreate ID")
            @PathVariable("recreateId") String recreateId) {
        return recreateService.getRecreateOrderDetails(UUID.fromString(recreateId), getUserId());
    }

    private String getUserId() {
        return userServiceHelper.getCurrentUserId();
    }

    private Map<String, Object> getRecreateProcessVariable(
            ServiceDeploymentEntity deployedService,
            String userId) {
        Map<String, Object> variable = new HashMap<>();
        variable.put(RecreateConstants.ID, deployedService.getDeployRequest().getServiceId());
        variable.put(RecreateConstants.RECREATE_REQUEST, deployedService.getDeployRequest());
        variable.put(RecreateConstants.USER_ID, userId);
        return variable;
    }

}
