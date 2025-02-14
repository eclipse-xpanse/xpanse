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
import java.util.List;
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
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateQueryModel;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.deployment.ServiceDeploymentEntityHandler;
import org.eclipse.xpanse.modules.deployment.ServiceOrderManager;
import org.eclipse.xpanse.modules.deployment.serviceporting.consts.ServicePortingConstants;
import org.eclipse.xpanse.modules.logging.CustomRequestIdGenerator;
import org.eclipse.xpanse.modules.models.common.enums.UserOperation;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.BillingModeNotSupported;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceLockedException;
import org.eclipse.xpanse.modules.models.service.enums.Handler;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateUnavailableException;
import org.eclipse.xpanse.modules.models.workflow.serviceporting.ServicePortingRequest;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
import org.eclipse.xpanse.modules.workflow.utils.WorkflowUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** REST interface methods for Service Porting. */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
@ConditionalOnProperty(name = "enable.agent.api.only", havingValue = "false", matchIfMissing = true)
public class ServicePortingApi {

    @Resource private ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;
    @Resource private UserServiceHelper userServiceHelper;
    @Resource private WorkflowUtils workflowUtils;
    @Resource private ServiceTemplateStorage serviceTemplateStorage;
    @Resource private ServiceOrderManager serviceOrderManager;

    /**
     * Create a job to port the deployed service.
     *
     * @return response
     */
    @Tag(name = "ServicePorting", description = "APIs to manage the service porting.")
    @Operation(description = "Create a job to port the deployed service.")
    @PostMapping(value = "/services/porting", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public ServiceOrder port(@Valid @RequestBody ServicePortingRequest servicePortingRequest) {
        validateData(servicePortingRequest);
        ServiceDeploymentEntity deployServiceEntity =
                this.serviceDeploymentEntityHandler.getServiceDeploymentEntity(
                        servicePortingRequest.getOriginalServiceId());
        String userId = this.userServiceHelper.getCurrentUserId();
        if (!StringUtils.equals(userId, deployServiceEntity.getUserId())) {
            String errorMsg =
                    String.format(
                            "No permission to %s owned by other users.",
                            UserOperation.PORT_SERVICE.toValue());
            log.error(errorMsg);
            throw new AccessDeniedException(errorMsg);
        }
        if (Objects.nonNull(deployServiceEntity.getLockConfig())
                && deployServiceEntity.getLockConfig().isModifyLocked()) {
            String errorMsg =
                    String.format(
                            "Service with id %s is locked from porting.",
                            servicePortingRequest.getOriginalServiceId());
            throw new ServiceLockedException(errorMsg);
        }
        servicePortingRequest.setUserId(userId);
        DeployTask servicePortingTask = getServicePortingTask(servicePortingRequest);
        ServiceOrderEntity servicePortingOrderEntity =
                serviceOrderManager.storeNewServiceOrderEntity(
                        servicePortingTask, deployServiceEntity, Handler.WORKFLOW);
        Map<String, Object> variable =
                getServicePortingProcessVariable(servicePortingRequest, servicePortingOrderEntity);
        ProcessInstance instance =
                workflowUtils.startProcessWithVariables(
                        ServicePortingConstants.PROCESS_KEY, variable);
        servicePortingOrderEntity.setWorkflowId(instance.getProcessInstanceId());
        ServiceOrderEntity updatedOrderEntity =
                serviceOrderManager.startOrderProgress(servicePortingOrderEntity);
        return new ServiceOrder(
                updatedOrderEntity.getOrderId(),
                (UUID) variable.get(ServicePortingConstants.NEW_SERVICE_ID));
    }

    private void validateData(ServicePortingRequest servicePortingRequest) {
        ServiceTemplateQueryModel queryModel =
                ServiceTemplateQueryModel.builder()
                        .category(servicePortingRequest.getCategory())
                        .csp(servicePortingRequest.getCsp())
                        .serviceName(servicePortingRequest.getServiceName())
                        .serviceVersion(servicePortingRequest.getVersion())
                        .serviceHostingType(servicePortingRequest.getServiceHostingType())
                        .build();
        List<ServiceTemplateEntity> existingServiceTemplates =
                serviceTemplateStorage.listServiceTemplates(queryModel);
        ServiceTemplateEntity existingTemplate =
                existingServiceTemplates.stream()
                        .filter(
                                serviceTemplate ->
                                        serviceTemplate.getIsAvailableInCatalog()
                                                && Objects.nonNull(serviceTemplate.getOcl()))
                        .findFirst()
                        .orElse(null);
        if (Objects.isNull(existingTemplate)) {
            String errorMsg = "No service template is available to be used to port service";
            log.error(errorMsg);
            throw new ServiceTemplateUnavailableException(errorMsg);
        }
        if (!existingTemplate
                .getOcl()
                .getBilling()
                .getBillingModes()
                .contains(servicePortingRequest.getBillingMode())) {
            String errorMsg =
                    String.format(
                            "The service template with id %s does not support billing mode %s.",
                            existingTemplate.getId(), servicePortingRequest.getBillingMode());
            log.error(errorMsg);
            throw new BillingModeNotSupported(errorMsg);
        }
    }

    private DeployTask getServicePortingTask(ServicePortingRequest servicePortingRequest) {
        DeployTask servicePortingTask = new DeployTask();
        servicePortingTask.setOrderId(CustomRequestIdGenerator.generateOrderId());
        servicePortingTask.setTaskType(ServiceOrderType.PORT);
        servicePortingTask.setServiceId(servicePortingRequest.getOriginalServiceId());
        servicePortingTask.setOriginalServiceId(servicePortingRequest.getOriginalServiceId());
        servicePortingTask.setUserId(servicePortingRequest.getUserId());
        servicePortingTask.setRequest(servicePortingRequest);
        return servicePortingTask;
    }

    private Map<String, Object> getServicePortingProcessVariable(
            ServicePortingRequest servicePortingRequest,
            ServiceOrderEntity servicePortingOrderEntity) {
        Map<String, Object> variable = new HashMap<>();
        variable.put(
                ServicePortingConstants.SERVICE_PORTING_ORDER_ID,
                servicePortingOrderEntity.getOrderId());
        variable.put(
                ServicePortingConstants.ORIGINAL_SERVICE_ID,
                servicePortingRequest.getOriginalServiceId());
        variable.put(ServicePortingConstants.NEW_SERVICE_ID, UUID.randomUUID());
        variable.put(ServicePortingConstants.SERVICE_PORTING_REQUEST, servicePortingRequest);
        variable.put(ServicePortingConstants.USER_ID, servicePortingRequest.getUserId());
        variable.put(ServicePortingConstants.DEPLOY_RETRY_NUM, 0);
        variable.put(ServicePortingConstants.DESTROY_RETRY_NUM, 0);
        return variable;
    }
}
