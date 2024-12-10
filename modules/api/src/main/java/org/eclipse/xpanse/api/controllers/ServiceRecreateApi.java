/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;


import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.deployment.ServiceDeploymentEntityHandler;
import org.eclipse.xpanse.modules.deployment.ServiceOrderManager;
import org.eclipse.xpanse.modules.deployment.recreate.consts.RecreateConstants;
import org.eclipse.xpanse.modules.logging.CustomRequestIdGenerator;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceLockedException;
import org.eclipse.xpanse.modules.models.service.enums.Handler;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.eclipse.xpanse.modules.workflow.utils.WorkflowUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    private ServiceOrderManager serviceOrderManager;

    @Resource
    private UserServiceHelper userServiceHelper;

    @Resource
    private WorkflowUtils workflowUtils;


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
    public ServiceOrder recreateService(@Valid @PathVariable("serviceId") String serviceId) {
        ServiceDeploymentEntity serviceDeploymentEntity = this.serviceDeploymentEntityHandler
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
                .equals(ServiceDeploymentState.DESTROY_FAILED)
                && !serviceDeploymentEntity.getServiceDeploymentState()
                .equals(ServiceDeploymentState.MODIFICATION_FAILED)
                && !serviceDeploymentEntity.getServiceDeploymentState()
                .equals(ServiceDeploymentState.MODIFICATION_SUCCESSFUL)) {
            throw new InvalidServiceStateException(
                    String.format("Service %s with the state %s is not allowed to recreate.",
                            serviceDeploymentEntity.getId(),
                            serviceDeploymentEntity.getServiceDeploymentState()));
        }

        // prepare parent recreate service order entity
        DeployTask recreateTask = getRecreateTask(serviceDeploymentEntity);
        ServiceOrderEntity recreateOrderEntity =
                serviceOrderManager.storeNewServiceOrderEntity(recreateTask,
                        serviceDeploymentEntity, Handler.WORKFLOW);

        // prepare recreate process variables
        Map<String, Object> variable =
                getRecreateProcessVariable(serviceDeploymentEntity, recreateOrderEntity);
        ProcessInstance instance =
                workflowUtils.startProcess(RecreateConstants.PROCESS_KEY, variable);

        recreateOrderEntity.setWorkflowId(instance.getProcessInstanceId());
        ServiceOrderEntity updatedRecreateOrderEntity =
                serviceOrderManager.startOrderProgress(recreateOrderEntity);

        return new ServiceOrder(updatedRecreateOrderEntity.getOrderId(),
                updatedRecreateOrderEntity.getOriginalServiceId());
    }

    private DeployTask getRecreateTask(ServiceDeploymentEntity serviceDeploymentEntity) {
        DeployTask recreateTask = new DeployTask();
        recreateTask.setOrderId(CustomRequestIdGenerator.generateOrderId());
        recreateTask.setTaskType(ServiceOrderType.RECREATE);
        recreateTask.setServiceId(serviceDeploymentEntity.getId());
        recreateTask.setOriginalServiceId(serviceDeploymentEntity.getId());
        recreateTask.setUserId(getUserId());
        recreateTask.setRequest(serviceDeploymentEntity.getDeployRequest());
        return recreateTask;
    }

    private String getUserId() {
        return this.userServiceHelper.getCurrentUserId();
    }

    private Map<String, Object> getRecreateProcessVariable(
            ServiceDeploymentEntity deployedService,
            ServiceOrderEntity recreateOrderEntity) {
        Map<String, Object> variable = new HashMap<>();
        variable.put(RecreateConstants.SERVICE_ID, deployedService.getId());
        variable.put(RecreateConstants.RECREATE_ORDER_ID, recreateOrderEntity.getOrderId());
        variable.put(RecreateConstants.RECREATE_REQUEST, deployedService.getDeployRequest());
        variable.put(RecreateConstants.USER_ID, recreateOrderEntity.getUserId());
        variable.put(RecreateConstants.DEPLOY_RETRY_NUM, 0);
        variable.put(RecreateConstants.DESTROY_RETRY_NUM, 0);
        return variable;
    }

}
