/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import jakarta.annotation.Resource;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.servicemigration.ServiceMigrationEntity;
import org.eclipse.xpanse.modules.deployment.DeployResultManager;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityToDeployTaskConverter;
import org.eclipse.xpanse.modules.deployment.ResourceHandlerManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformResult;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.deployment.migration.MigrationService;
import org.eclipse.xpanse.modules.deployment.migration.consts.MigrateConstants;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployerTaskStatus;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.workflow.utils.WorkflowUtils;
import org.springframework.stereotype.Component;

/**
 * Bean for managing deployment and destroy callback functions.
 */
@Slf4j
@Component
public class TerraformDeploymentResultCallbackManager {

    @Resource
    DeployServiceEntityHandler deployServiceEntityHandler;

    @Resource
    ResourceHandlerManager resourceHandlerManager;

    @Resource
    DeployResultManager deployResultManager;

    @Resource
    private DeployServiceEntityToDeployTaskConverter deployServiceEntityToDeployTaskConverter;

    @Resource
    private DeployService deployService;

    @Resource
    private MigrationService migrationService;

    @Resource
    private WorkflowUtils workflowUtils;

    /**
     * Callback method after deployment is complete.
     */
    public void deployCallback(String taskId, TerraformResult result) {
        log.info("Update database entity with id:{} with terraform deploy callback result.",
                taskId);
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(UUID.fromString(taskId));
        DeployResult deployResult = handlerCallbackDeployResult(result);
        deployResult.setId(UUID.fromString(taskId));
        if (StringUtils.isNotBlank(result.getTerraformState())) {
            resourceHandlerManager.getResourceHandler(
                    deployServiceEntity.getCsp(),
                    deployServiceEntity.getDeployRequest().getOcl().getDeployment()
                            .getKind()).handler(deployResult);
        }
        DeployServiceEntity updatedDeployServiceEntity =
                deployResultManager.updateDeployServiceEntityWithDeployResult(deployResult,
                        deployServiceEntity);
        if (ServiceDeploymentState.DEPLOY_FAILED
                == updatedDeployServiceEntity.getServiceDeploymentState()) {
            DeployTask deployTask =
                    deployServiceEntityToDeployTaskConverter.getDeployTaskByStoredService(
                            updatedDeployServiceEntity);
            deployService.rollbackOnDeploymentFailure(deployTask, updatedDeployServiceEntity);
        }

        ServiceMigrationEntity serviceMigrationEntity =
                migrationService.getServiceMigrationEntityByNewServiceId(taskId);
        if (Objects.nonNull(serviceMigrationEntity)) {
            workflowUtils.completeReceiveTask(serviceMigrationEntity.getMigrationId().toString(),
                    MigrateConstants.MIGRATION_DEPLOY_RECEIVE_TASK_ACTIVITY_ID);
        }
    }

    /**
     * Callback method after the service is destroyed.
     */
    public void destroyCallback(String taskId, TerraformResult result) {
        log.info("Update database entity with id:{} with terraform destroy callback result.",
                taskId);
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(UUID.fromString(taskId));
        DeployResult destroyResult = handlerCallbackDestroyResult(result);
        destroyResult.setId(UUID.fromString(taskId));
        if (StringUtils.isNotBlank(result.getTerraformState())) {
            resourceHandlerManager.getResourceHandler(
                    deployServiceEntity.getCsp(),
                    deployServiceEntity.getDeployRequest().getOcl().getDeployment()
                            .getKind()).handler(destroyResult);
        }
        try {
            deployResultManager.updateDeployServiceEntityWithDestroyResult(destroyResult,
                    deployServiceEntity, false);
        } catch (RuntimeException e) {
            log.error("Update database entity with id:{} with terraform destroy callback result "
                    + "failed.", taskId, e);
        }

        ServiceMigrationEntity serviceMigrationEntity =
                migrationService.getServiceMigrationEntityByOldServiceId(taskId);
        if (Objects.nonNull(serviceMigrationEntity)) {
            workflowUtils.completeReceiveTask(serviceMigrationEntity.getMigrationId().toString(),
                    MigrateConstants.MIGRATION_DESTROY_RECEIVE_TASK_ACTIVITY_ID);
        }
    }

    private DeployResult handlerCallbackDeployResult(TerraformResult result) {
        DeployResult deployResult = new DeployResult();
        if (Boolean.TRUE.equals(result.getCommandSuccessful())) {
            deployResult.setState(DeployerTaskStatus.DEPLOY_SUCCESS);
        } else {
            deployResult.setState(DeployerTaskStatus.DEPLOY_FAILED);
            deployResult.setMessage(result.getCommandStdError());
        }
        deployResult.getPrivateProperties()
                .put(TfResourceTransUtils.STATE_FILE_NAME, result.getTerraformState());
        if (Objects.nonNull(result.getImportantFileContentMap())) {
            deployResult.getPrivateProperties().putAll(result.getImportantFileContentMap());
        }
        return deployResult;
    }

    private DeployResult handlerCallbackDestroyResult(TerraformResult result) {
        DeployResult deployResult = new DeployResult();
        if (Boolean.TRUE.equals(result.getCommandSuccessful())) {
            deployResult.setState(DeployerTaskStatus.DESTROY_SUCCESS);
        } else {
            deployResult.setState(DeployerTaskStatus.DEPLOY_FAILED);
            deployResult.setMessage(result.getCommandStdError());
        }
        deployResult.getPrivateProperties()
                .put(TfResourceTransUtils.STATE_FILE_NAME, result.getTerraformState());
        if (Objects.nonNull(result.getImportantFileContentMap())) {
            deployResult.getPrivateProperties().putAll(result.getImportantFileContentMap());
        }
        return deployResult;
    }
}
