/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_USER;

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
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.deployment.ServiceDeploymentEntityHandler;
import org.eclipse.xpanse.modules.deployment.ServiceOrderManager;
import org.eclipse.xpanse.modules.deployment.recreate.consts.RecreateConstants;
import org.eclipse.xpanse.modules.models.common.enums.UserOperation;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceLockedException;
import org.eclipse.xpanse.modules.models.service.enums.Handler;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateUnavailableException;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
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

/** REST interface methods for Service Recreate. */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
@ConditionalOnProperty(
        name = "xpanse.agent-api.enable-agent-api-only",
        havingValue = "false",
        matchIfMissing = true)
public class ServiceRecreateApi {

    @Resource private ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;
    @Resource private ServiceOrderManager serviceOrderManager;
    @Resource private UserServiceHelper userServiceHelper;
    @Resource private WorkflowUtils workflowUtils;

    /**
     * Create a job to recreate the deployed service.
     *
     * @return response
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description = "Create a job to recreate the deployed service.")
    @PutMapping(
            value = "/services/recreate/{serviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromServiceId", paramTypes = UUID.class)
    public ServiceOrder recreateService(@Valid @PathVariable("serviceId") UUID serviceId) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                this.serviceDeploymentEntityHandler.getServiceDeploymentEntity(serviceId);
        String userId = userServiceHelper.getCurrentUserId();
        if (!StringUtils.equals(userId, serviceDeploymentEntity.getUserId())) {
            String errorMsg =
                    String.format(
                            "No permission to %s owned by other users.",
                            UserOperation.RECREATE_SERVICE.toValue());
            log.error(errorMsg);
            throw new AccessDeniedException(errorMsg);
        }

        if (Objects.nonNull(serviceDeploymentEntity.getLockConfig())
                && serviceDeploymentEntity.getLockConfig().isModifyLocked()) {
            String errorMsg =
                    String.format("Service with id %s is locked from recreate.", serviceId);
            throw new ServiceLockedException(errorMsg);
        }

        ServiceTemplateEntity existingServiceTemplate =
                serviceDeploymentEntity.getServiceTemplateEntity();
        if (!existingServiceTemplate.getIsAvailableInCatalog()) {
            String errorMsg =
                    String.format(
                            "Service template %s is unavailable to be used to recreate service.",
                            existingServiceTemplate.getId());
            log.error(errorMsg);
            throw new ServiceTemplateUnavailableException(errorMsg);
        }
        // prepare parent recreate service order entity
        this.serviceDeploymentEntityHandler.validateServiceDeploymentStateForOrderType(
                serviceDeploymentEntity, ServiceOrderType.RECREATE);
        DeployTask recreateTask = getRecreateTask(serviceDeploymentEntity);
        ServiceOrderEntity recreateOrderEntity =
                serviceOrderManager.storeNewServiceOrderEntity(
                        recreateTask, serviceDeploymentEntity, Handler.WORKFLOW);
        // prepare recreate process variables
        Map<String, Object> variables = getRecreateProcessVariables(recreateTask);
        ProcessInstance instance =
                workflowUtils.startProcessWithVariables(RecreateConstants.PROCESS_KEY, variables);
        recreateOrderEntity.setWorkflowId(instance.getProcessInstanceId());
        serviceOrderManager.startOrderProgress(recreateOrderEntity);
        return new ServiceOrder(recreateTask.getOrderId(), recreateTask.getServiceId());
    }

    private DeployTask getRecreateTask(ServiceDeploymentEntity serviceDeploymentEntity) {
        DeployTask recreateTask = new DeployTask();
        recreateTask.setTaskType(ServiceOrderType.RECREATE);
        recreateTask.setServiceId(serviceDeploymentEntity.getId());
        recreateTask.setOriginalServiceId(serviceDeploymentEntity.getId());
        recreateTask.setUserId(serviceDeploymentEntity.getUserId());
        return recreateTask;
    }

    private Map<String, Object> getRecreateProcessVariables(DeployTask recreateTask) {
        Map<String, Object> variable = new HashMap<>();
        variable.put(RecreateConstants.SERVICE_ID, recreateTask.getServiceId());
        variable.put(RecreateConstants.RECREATE_ORDER_ID, recreateTask.getOrderId());
        variable.put(RecreateConstants.RECREATE_REQUEST, recreateTask.getDeployRequest());
        variable.put(RecreateConstants.USER_ID, recreateTask.getUserId());
        variable.put(RecreateConstants.DEPLOY_RETRY_NUM, 0);
        variable.put(RecreateConstants.DESTROY_RETRY_NUM, 0);
        return variable;
    }
}
