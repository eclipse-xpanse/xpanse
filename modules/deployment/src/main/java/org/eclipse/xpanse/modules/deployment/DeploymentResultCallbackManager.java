/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformResult;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.enums.TerraformExecState;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployment;
import org.springframework.stereotype.Component;

/**
 * Bean for managing deployment and destroy callback functions.
 */
@Slf4j
@Component
public class DeploymentResultCallbackManager {

    public static final String STATE_FILE_NAME = "terraform.tfstate";

    @Resource
    DeployServiceEntityHandler deployServiceEntityHandler;

    @Resource
    ResourceHandlerManager resourceHandlerManager;

    @Resource
    DeployResultManager deployResultManager;

    @Resource
    private DeployerKindManager deployerKindManager;

    @Resource
    private DeployServiceEntityToDeployTaskConverter deployServiceEntityToDeployTaskConverter;

    @Resource
    private DeployService deployService;

    /**
     * Callback method after deployment is complete.
     */
    public void deployCallback(String taskId, TerraformResult result) {
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(UUID.fromString(taskId));
        DeployResult deployResult = handlerCallbackDeployResult(result);
        deployResult.setId(UUID.fromString(taskId));
        if (StringUtils.isNotBlank(result.getTerraformState())) {
            resourceHandlerManager.getResourceHandler(deployServiceEntity.getCsp())
                    .handler(deployResult);
        }
        DeployServiceEntity updatedDeployServiceEntity =
                deployResultManager.updateDeployServiceEntityWithDeployResult(
                        deployResult, deployServiceEntity);
        if (ServiceDeploymentState.DEPLOY_FAILED
                == updatedDeployServiceEntity.getServiceDeploymentState()) {
            DeployTask deployTask =
                    deployServiceEntityToDeployTaskConverter.getDeployTaskByStoredService(
                            updatedDeployServiceEntity);
            Deployment deployment =
                    deployerKindManager.getDeployment(
                            updatedDeployServiceEntity
                                    .getDeployRequest()
                                    .getOcl()
                                    .getDeployment()
                                    .getKind());
            deployService.rollbackOnDeploymentFailure(
                    deployment, deployTask, updatedDeployServiceEntity);
        }

    }

    private DeployResult handlerCallbackDeployResult(TerraformResult result) {
        DeployResult deployResult = new DeployResult();
        if (Boolean.TRUE.equals(result.getCommandSuccessful())) {
            deployResult.setState(TerraformExecState.DEPLOY_SUCCESS);
        } else {
            deployResult.setState(TerraformExecState.DEPLOY_FAILED);
            deployResult.setMessage(result.getCommandStdError());
        }
        deployResult.getPrivateProperties().put(STATE_FILE_NAME, result.getTerraformState());
        if (Objects.nonNull(result.getImportantFileContentMap())) {
            deployResult.getPrivateProperties().putAll(result.getImportantFileContentMap());
        }
        return deployResult;
    }

    /**
     * Callback method after the service is destroyed.
     */
    public void destroyCallback(String taskId, TerraformResult result) {
        log.info("Update database entity with id:{} with destroy  callback result.", taskId);
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(UUID.fromString(taskId));
        DeployResult destroyResult = handlerCallbackDestroyResult(result);
        destroyResult.setId(UUID.fromString(taskId));
        if (StringUtils.isNotBlank(result.getTerraformState())) {
            resourceHandlerManager.getResourceHandler(
                    deployServiceEntity.getCsp()).handler(destroyResult);
        }
        try {
            deployResultManager.updateDeployServiceEntityWithDestroyResult(
                    destroyResult, deployServiceEntity, false);
        } catch (RuntimeException e) {
            log.info("Update database entity with id:{} with destroy callback result failed.",
                    taskId, e);
        }
    }

    private DeployResult handlerCallbackDestroyResult(TerraformResult result) {
        DeployResult deployResult = new DeployResult();
        if (Boolean.TRUE.equals(result.getCommandSuccessful())) {
            deployResult.setState(TerraformExecState.DESTROY_SUCCESS);
        } else {
            deployResult.setState(TerraformExecState.DEPLOY_FAILED);
            deployResult.setMessage(result.getCommandStdError());
        }
        deployResult.getPrivateProperties().put(STATE_FILE_NAME, result.getTerraformState());
        if (Objects.nonNull(result.getImportantFileContentMap())) {
            deployResult.getPrivateProperties().putAll(result.getImportantFileContentMap());
        }
        return deployResult;
    }
}
