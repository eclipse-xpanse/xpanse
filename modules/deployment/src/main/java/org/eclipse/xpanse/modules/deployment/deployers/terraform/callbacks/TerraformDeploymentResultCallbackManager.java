/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.callbacks;

import jakarta.annotation.Resource;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.servicemigration.ServiceMigrationEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.database.servicerecreate.ServiceRecreateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.deployment.DeployResultManager;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityConverter;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.ResourceHandlerManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformResult;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.deployment.migration.MigrationService;
import org.eclipse.xpanse.modules.deployment.migration.consts.MigrateConstants;
import org.eclipse.xpanse.modules.deployment.recreate.RecreateService;
import org.eclipse.xpanse.modules.deployment.recreate.consts.RecreateConstants;
import org.eclipse.xpanse.modules.models.service.enums.DeployerTaskStatus;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScenario;
import org.eclipse.xpanse.modules.workflow.utils.WorkflowUtils;
import org.springframework.stereotype.Component;

/**
 * Bean for managing deployer terraform callback functions.
 */
@Slf4j
@Component
public class TerraformDeploymentResultCallbackManager {

    @Resource
    private DeployServiceEntityHandler deployServiceEntityHandler;
    @Resource
    private ResourceHandlerManager resourceHandlerManager;
    @Resource
    private DeployResultManager deployResultManager;
    @Resource
    private DeployServiceEntityConverter deployServiceEntityConverter;
    @Resource
    private DeployService deployService;
    @Resource
    private MigrationService migrationService;
    @Resource
    private RecreateService recreateService;
    @Resource
    private WorkflowUtils workflowUtils;
    @Resource
    private ServiceTemplateStorage serviceTemplateStorage;
    @Resource
    private ServiceOrderStorage serviceOrderStorage;

    /**
     * Callback method after the deployment task is completed.
     */
    public void deployCallback(UUID serviceId, TerraformResult result) {
        DeployResult deployResult =
                getDeployResult(serviceId, result, DeploymentScenario.DEPLOY);
        DeployServiceEntity updatedDeployServiceEntity = updateDeployServiceEntity(deployResult);
        if (ServiceDeploymentState.DEPLOY_FAILED
                == updatedDeployServiceEntity.getServiceDeploymentState()) {
            DeployTask rollbackTask =
                    deployServiceEntityConverter.getDeployTaskByStoredService(
                            updatedDeployServiceEntity);
            rollbackTask.setOrderId(result.getRequestId());
            deployService.rollbackOnDeploymentFailure(rollbackTask, updatedDeployServiceEntity);
        }

        ServiceMigrationEntity serviceMigrationEntity =
                migrationService.getServiceMigrationEntityByNewServiceId(serviceId);
        if (Objects.nonNull(serviceMigrationEntity)) {
            workflowUtils.completeReceiveTask(serviceMigrationEntity.getMigrationId().toString(),
                    MigrateConstants.MIGRATION_DEPLOY_RECEIVE_TASK_ACTIVITY_ID);
        }
        ServiceRecreateEntity serviceRecreateEntity =
                recreateService.getServiceRecreateEntityByNewServiceId(serviceId);
        if (Objects.nonNull(serviceRecreateEntity)) {
            workflowUtils.completeReceiveTask(serviceRecreateEntity.getRecreateId().toString(),
                    RecreateConstants.RECREATE_DEPLOY_RECEIVE_TASK_ACTIVITY_ID);
        }
        updateServiceOrderEntity(deployResult);
    }

    /**
     * Callback method after the modification task is completed.
     */
    public void modifyCallback(UUID serviceId, TerraformResult terraformResult) {
        DeployResult deployResult =
                getDeployResult(serviceId, terraformResult, DeploymentScenario.MODIFY);
        updateDeployServiceEntity(deployResult);
        updateServiceOrderEntity(deployResult);
    }

    private void updateServiceOrderEntity(DeployResult deployResult) {
        ServiceOrderEntity serviceOrderEntity =
                serviceOrderStorage.getEntityById(deployResult.getOrderId());
        deployResultManager.updateServiceOrderTaskWithDeployResult(deployResult,
                serviceOrderEntity);
    }

    /**
     * Callback method after the destroy/rollback/purge task is completed.
     */
    public void destroyCallback(UUID serviceId, TerraformResult result,
                                DeploymentScenario scenario) {
        DeployResult deployResult = getDeployResult(serviceId, result, scenario);
        updateDeployServiceEntity(deployResult);
        ServiceMigrationEntity serviceMigrationEntity =
                migrationService.getServiceMigrationEntityByOldServiceId(serviceId);
        if (Objects.nonNull(serviceMigrationEntity)) {
            workflowUtils.completeReceiveTask(serviceMigrationEntity.getMigrationId().toString(),
                    MigrateConstants.MIGRATION_DESTROY_RECEIVE_TASK_ACTIVITY_ID);
        }
        ServiceRecreateEntity serviceRecreateEntity =
                recreateService.getServiceRecreateEntityByServiceId(serviceId);
        if (Objects.nonNull(serviceRecreateEntity)) {
            workflowUtils.completeReceiveTask(serviceRecreateEntity.getRecreateId().toString(),
                    RecreateConstants.RECREATE_DESTROY_RECEIVE_TASK_ACTIVITY_ID);
        }
        updateServiceOrderEntity(deployResult);
    }

    private DeployServiceEntity updateDeployServiceEntity(DeployResult deployResult) {
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(deployResult.getServiceId());
        if (StringUtils.isNotBlank(deployResult.getTfStateContent())) {
            ServiceTemplateEntity serviceTemplateEntity =
                    serviceTemplateStorage.getServiceTemplateById(
                            deployServiceEntity.getServiceTemplateId());
            DeployerKind deployerKind = serviceTemplateEntity.getOcl().getDeployment()
                    .getDeployerTool().getKind();
            resourceHandlerManager.getResourceHandler(deployServiceEntity.getCsp(), deployerKind)
                    .handler(deployResult);
        }
        return deployResultManager.updateDeployServiceEntityWithDeployResult(deployResult,
                deployServiceEntity);
    }

    private DeployResult getDeployResult(UUID serviceId, TerraformResult result,
                                         DeploymentScenario scenario) {
        DeployResult deployResult = new DeployResult();
        deployResult.setOrderId(result.getRequestId());
        deployResult.setServiceId(serviceId);
        deployResult.setDeployerVersionUsed(result.getTerraformVersionUsed());
        if (Boolean.FALSE.equals(result.getCommandSuccessful())) {
            deployResult.setIsTaskSuccessful(false);
            deployResult.setMessage(result.getCommandStdError());
        } else {
            deployResult.setIsTaskSuccessful(true);
            deployResult.setMessage(null);
        }
        deployResult.setTfStateContent(result.getTerraformState());
        deployResult.setState(getDeployerTaskStatus(scenario, result.getCommandSuccessful()));
        deployResult.getPrivateProperties()
                .put(TfResourceTransUtils.STATE_FILE_NAME, result.getTerraformState());
        if (Objects.nonNull(result.getImportantFileContentMap())) {
            deployResult.getPrivateProperties().putAll(result.getImportantFileContentMap());
        }
        return deployResult;
    }

    private DeployerTaskStatus getDeployerTaskStatus(DeploymentScenario scenario,
                                                     Boolean isCommandSuccessful) {
        return switch (scenario) {
            case DEPLOY -> isCommandSuccessful ? DeployerTaskStatus.DEPLOY_SUCCESS
                    : DeployerTaskStatus.DEPLOY_FAILED;
            case MODIFY -> isCommandSuccessful ? DeployerTaskStatus.MODIFICATION_SUCCESSFUL
                    : DeployerTaskStatus.MODIFICATION_FAILED;
            case DESTROY -> isCommandSuccessful ? DeployerTaskStatus.DESTROY_SUCCESS
                    : DeployerTaskStatus.DESTROY_FAILED;
            case ROLLBACK -> isCommandSuccessful ? DeployerTaskStatus.ROLLBACK_SUCCESS
                    : DeployerTaskStatus.ROLLBACK_FAILED;
            case PURGE -> isCommandSuccessful ? DeployerTaskStatus.PURGE_SUCCESS
                    : DeployerTaskStatus.PURGE_FAILED;
        };
    }
}
