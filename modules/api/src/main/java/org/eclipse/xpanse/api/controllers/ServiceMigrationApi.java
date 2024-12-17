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
import org.eclipse.xpanse.modules.deployment.migration.consts.MigrateConstants;
import org.eclipse.xpanse.modules.logging.CustomRequestIdGenerator;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.BillingModeNotSupported;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.EulaNotAccepted;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceLockedException;
import org.eclipse.xpanse.modules.models.service.enums.Handler;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.models.workflow.migrate.MigrateRequest;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
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

/** REST interface methods for Service Migration. */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
@ConditionalOnProperty(name = "enable.agent.api.only", havingValue = "false", matchIfMissing = true)
public class ServiceMigrationApi {

    @Resource private ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;
    @Resource private UserServiceHelper userServiceHelper;
    @Resource private WorkflowUtils workflowUtils;
    @Resource private ServiceTemplateStorage serviceTemplateStorage;
    @Resource private ServiceOrderManager serviceOrderManager;

    /**
     * Create a job to migrate the deployed service.
     *
     * @return response
     */
    @Tag(name = "Migration", description = "APIs to manage the service migration.")
    @Operation(description = "Create a job to migrate the deployed service.")
    @PostMapping(value = "/services/migration", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public ServiceOrder migrate(@Valid @RequestBody MigrateRequest migrateRequest) {
        validateData(migrateRequest);
        ServiceDeploymentEntity deployServiceEntity =
                this.serviceDeploymentEntityHandler.getServiceDeploymentEntity(
                        migrateRequest.getOriginalServiceId());
        String userId = this.userServiceHelper.getCurrentUserId();
        if (!StringUtils.equals(userId, deployServiceEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to migrate services belonging to other users.");
        }
        if (Objects.nonNull(deployServiceEntity.getLockConfig())
                && deployServiceEntity.getLockConfig().isModifyLocked()) {
            String errorMsg =
                    String.format(
                            "Service with id %s is locked from migration.",
                            migrateRequest.getOriginalServiceId());
            throw new ServiceLockedException(errorMsg);
        }
        migrateRequest.setUserId(userId);
        DeployTask migrateTask = getMigrateTask(migrateRequest);
        ServiceOrderEntity migrateOrderEntity =
                serviceOrderManager.storeNewServiceOrderEntity(
                        migrateTask, deployServiceEntity, Handler.WORKFLOW);
        Map<String, Object> variable =
                getMigrateProcessVariable(migrateRequest, migrateOrderEntity);
        ProcessInstance instance =
                workflowUtils.startProcess(MigrateConstants.PROCESS_KEY, variable);
        migrateOrderEntity.setWorkflowId(instance.getProcessInstanceId());
        ServiceOrderEntity updatedOrderEntity =
                serviceOrderManager.startOrderProgress(migrateOrderEntity);
        return new ServiceOrder(
                updatedOrderEntity.getOrderId(),
                (UUID) variable.get(MigrateConstants.NEW_SERVICE_ID));
    }

    private void validateData(MigrateRequest migrateRequest) {
        ServiceTemplateQueryModel queryModel =
                ServiceTemplateQueryModel.builder()
                        .category(migrateRequest.getCategory())
                        .csp(migrateRequest.getCsp())
                        .serviceName(migrateRequest.getServiceName())
                        .serviceVersion(migrateRequest.getVersion())
                        .serviceHostingType(migrateRequest.getServiceHostingType())
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
                        .orElseThrow(
                                () ->
                                        new ServiceTemplateNotRegistered(
                                                "No available service templates found"));
        if (StringUtils.isNotBlank(existingTemplate.getOcl().getEula())
                && !migrateRequest.isEulaAccepted()) {
            log.error("Service not accepted Eula.");
            throw new EulaNotAccepted("Service not accepted Eula.");
        }
        if (!existingTemplate
                .getOcl()
                .getBilling()
                .getBillingModes()
                .contains(migrateRequest.getBillingMode())) {
            String errorMsg =
                    String.format(
                            "The service template with id %s does not support billing mode %s.",
                            existingTemplate.getId(), migrateRequest.getBillingMode());
            log.error(errorMsg);
            throw new BillingModeNotSupported(errorMsg);
        }
    }

    private DeployTask getMigrateTask(MigrateRequest migrateRequest) {
        DeployTask migrateTask = new DeployTask();
        migrateTask.setOrderId(CustomRequestIdGenerator.generateOrderId());
        migrateTask.setTaskType(ServiceOrderType.MIGRATE);
        migrateTask.setServiceId(migrateRequest.getOriginalServiceId());
        migrateTask.setOriginalServiceId(migrateRequest.getOriginalServiceId());
        migrateTask.setUserId(migrateRequest.getUserId());
        migrateTask.setRequest(migrateRequest);
        return migrateTask;
    }

    private Map<String, Object> getMigrateProcessVariable(
            MigrateRequest migrateRequest, ServiceOrderEntity migrateOrderEntity) {
        Map<String, Object> variable = new HashMap<>();
        variable.put(MigrateConstants.MIGRATE_ORDER_ID, migrateOrderEntity.getOrderId());
        variable.put(MigrateConstants.ORIGINAL_SERVICE_ID, migrateRequest.getOriginalServiceId());
        variable.put(MigrateConstants.NEW_SERVICE_ID, UUID.randomUUID());
        variable.put(MigrateConstants.MIGRATE_REQUEST, migrateRequest);
        variable.put(MigrateConstants.USER_ID, migrateRequest.getUserId());
        variable.put(MigrateConstants.DEPLOY_RETRY_NUM, 0);
        variable.put(MigrateConstants.DESTROY_RETRY_NUM, 0);
        return variable;
    }
}
