/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployerTaskStatus;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceState;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Bean to handle deployment result.
 */
@Component
@Slf4j
public class DeployResultManager {

    @Resource
    private DeployServiceEntityHandler deployServiceEntityHandler;

    @Resource
    private SensitiveDataHandler sensitiveDataHandler;

    /**
     * Method to update deployment result to DeployServiceEntity and store to database.
     *
     * @param deployResult Deployment Result.
     * @param storedEntity DB entity to be updated
     * @return updated entity.
     * @throws RuntimeException exception thrown in case of errors.
     */
    public DeployServiceEntity updateDeployServiceEntityWithDeployResult(
            DeployResult deployResult, DeployServiceEntity storedEntity)
            throws RuntimeException {
        if (Objects.nonNull(deployResult.getState()) && Objects.nonNull(storedEntity)) {
            log.info("Deploy task update deploy service entity with id:{}", deployResult.getId());
            DeployServiceEntity deployServiceEntityToFlush = new DeployServiceEntity();
            BeanUtils.copyProperties(storedEntity, deployServiceEntityToFlush);
            if (DeployerTaskStatus.DEPLOY_SUCCESS == deployResult.getState()) {
                deployServiceEntityToFlush.setServiceDeploymentState(
                        ServiceDeploymentState.DEPLOY_SUCCESS);
                deployServiceEntityToFlush.setServiceState(ServiceState.RUNNING);
            } else {
                deployServiceEntityToFlush.setServiceDeploymentState(
                        ServiceDeploymentState.DEPLOY_FAILED);
                deployServiceEntityToFlush.setServiceState(ServiceState.NOT_RUNNING);
                deployServiceEntityToFlush.setResultMessage(deployResult.getMessage());
            }
            updateDeployResourceEntity(deployResult, deployServiceEntityToFlush);
            return deployServiceEntityHandler.storeAndFlush(deployServiceEntityToFlush);
        } else {
            return storedEntity;
        }
    }

    private void updateDeployResourceEntity(DeployResult deployResult,
                                            DeployServiceEntity deployServiceEntity) {

        if (!CollectionUtils.isEmpty(deployServiceEntity.getProperties())) {
            deployServiceEntity.getProperties().clear();
        }
        if (!CollectionUtils.isEmpty(deployServiceEntity.getPrivateProperties())) {
            deployServiceEntity.getPrivateProperties().clear();
        }
        if (!CollectionUtils.isEmpty(deployServiceEntity.getDeployResourceList())) {
            deployServiceEntity.getDeployResourceList().clear();
        }

        if (!CollectionUtils.isEmpty(deployResult.getPrivateProperties())) {
            deployServiceEntity.setPrivateProperties(deployResult.getPrivateProperties());
        }
        if (!CollectionUtils.isEmpty(deployResult.getProperties())) {
            deployServiceEntity.setProperties(deployResult.getProperties());
        }
        if (!CollectionUtils.isEmpty(deployResult.getResources())) {
            deployServiceEntity.setDeployResourceList(
                    getDeployResourceEntityList(deployResult.getResources(),
                            deployServiceEntity));
        }
        sensitiveDataHandler.maskSensitiveFields(deployServiceEntity);
    }

    /**
     * Convert deploy resources to deploy resource entities.
     */
    private List<DeployResourceEntity> getDeployResourceEntityList(
            List<DeployResource> deployResources, DeployServiceEntity deployServiceEntity) {
        List<DeployResourceEntity> deployResourceEntities = new ArrayList<>();
        if (CollectionUtils.isEmpty(deployResources)) {
            return deployResourceEntities;
        }
        for (DeployResource resource : deployResources) {
            DeployResourceEntity deployResource = new DeployResourceEntity();
            BeanUtils.copyProperties(resource, deployResource);
            deployResource.setDeployService(deployServiceEntity);
            deployResourceEntities.add(deployResource);
        }
        return deployResourceEntities;
    }

    /**
     * Method to update destroy result to DeployServiceEntity and store to database.
     *
     * @param destroyResult Destroy Result.
     * @param deployServiceEntity DB entity to be updated
     * @param isCalledWhenRollback is service destroyed as part of rollback.
     * @return updated entity.
     * @throws RuntimeException exception thrown in case of errors.
     */
    public DeployServiceEntity updateDeployServiceEntityWithDestroyResult(
            DeployResult destroyResult,
            DeployServiceEntity deployServiceEntity,
            boolean isCalledWhenRollback)
            throws RuntimeException {
        if (Objects.nonNull(destroyResult) && Objects.nonNull(destroyResult.getState())) {
            log.info("Update stored deploy service entity by result of destroy task with id:{}",
                    destroyResult.getId());
            DeployServiceEntity deployServiceEntityToFlush = new DeployServiceEntity();
            BeanUtils.copyProperties(deployServiceEntity, deployServiceEntityToFlush);
            if (isCalledWhenRollback) {
                if (destroyResult.getState() == DeployerTaskStatus.DESTROY_SUCCESS) {
                    deployServiceEntityToFlush.setServiceDeploymentState(
                            ServiceDeploymentState.DEPLOY_FAILED);
                    deployServiceEntityToFlush.setServiceState(ServiceState.NOT_RUNNING);
                } else {
                    deployServiceEntityToFlush.setServiceDeploymentState(
                            ServiceDeploymentState.ROLLBACK_FAILED);
                    deployServiceEntityToFlush.setServiceState(ServiceState.RUNNING);
                }
            } else {
                if (destroyResult.getState() == DeployerTaskStatus.DESTROY_SUCCESS) {
                    deployServiceEntityToFlush.setServiceDeploymentState(
                            ServiceDeploymentState.DESTROY_SUCCESS);
                    deployServiceEntityToFlush.setServiceState(ServiceState.NOT_RUNNING);
                } else {
                    deployServiceEntityToFlush.setServiceDeploymentState(
                            ServiceDeploymentState.DESTROY_FAILED);
                    deployServiceEntityToFlush.setServiceState(ServiceState.RUNNING);
                    deployServiceEntityToFlush.setResultMessage(destroyResult.getMessage());
                }
            }
            updateDeployResourceEntity(destroyResult, deployServiceEntityToFlush);
            return deployServiceEntityHandler.storeAndFlush(deployServiceEntityToFlush);
        } else {
            return deployServiceEntity;
        }
    }
}
