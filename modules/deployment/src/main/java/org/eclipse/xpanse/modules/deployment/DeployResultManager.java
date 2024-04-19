/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
            DeployResult deployResult, DeployServiceEntity storedEntity) throws RuntimeException {
        if (Objects.nonNull(deployResult.getState()) && Objects.nonNull(storedEntity)) {
            log.info("Deploy task update deploy service entity with id:{}", deployResult.getId());
            DeployServiceEntity deployServiceEntityToStore = new DeployServiceEntity();
            BeanUtils.copyProperties(storedEntity, deployServiceEntityToStore);
            updateEntityWithDeployResult(deployResult, deployServiceEntityToStore);
            return deployServiceEntityHandler.storeAndFlush(deployServiceEntityToStore);
        } else {
            return storedEntity;
        }
    }

    private void updateEntityWithDeployResult(DeployResult deployResult,
                                              DeployServiceEntity deployServiceEntity) {

        if (StringUtils.isNotBlank(deployResult.getMessage())) {
            deployServiceEntity.setResultMessage(deployResult.getMessage());
        }
        deployServiceEntity.setServiceDeploymentState(
                getServiceDeploymentState(deployResult.getState()));

        DeployerTaskStatus deployerTaskStatus = deployResult.getState();

        updateServiceState(deployerTaskStatus, deployServiceEntity);

        boolean taskExecutedSuccess = deployerTaskStatus == DeployerTaskStatus.DESTROY_SUCCESS
                || deployerTaskStatus == DeployerTaskStatus.MODIFICATION_SUCCESSFUL
                || deployerTaskStatus == DeployerTaskStatus.ROLLBACK_SUCCESS
                || deployerTaskStatus == DeployerTaskStatus.PURGE_SUCCESS
                || deployerTaskStatus == DeployerTaskStatus.DEPLOY_SUCCESS;

        if (CollectionUtils.isEmpty(deployResult.getPrivateProperties())) {
            if (taskExecutedSuccess) {
                deployServiceEntity.getPrivateProperties().clear();
            }
        } else {
            deployServiceEntity.setPrivateProperties(deployResult.getPrivateProperties());
        }

        if (CollectionUtils.isEmpty(deployResult.getProperties())) {
            if (taskExecutedSuccess) {
                deployServiceEntity.getProperties().clear();
            }
        } else {
            deployServiceEntity.setProperties(deployResult.getProperties());
        }

        if (CollectionUtils.isEmpty(deployResult.getResources())) {
            if (taskExecutedSuccess) {
                deployServiceEntity.getDeployResourceList().clear();
            }
        } else {
            deployServiceEntity.setDeployResourceList(
                    getDeployResourceEntityList(deployResult.getResources(), deployServiceEntity));
        }
        sensitiveDataHandler.maskSensitiveFields(deployServiceEntity);
    }

    private void updateServiceState(DeployerTaskStatus state,
                                    DeployServiceEntity deployServiceEntity) {
        if (state == DeployerTaskStatus.DEPLOY_SUCCESS
                || state == DeployerTaskStatus.MODIFICATION_SUCCESSFUL) {
            deployServiceEntity.setServiceState(ServiceState.RUNNING);
            deployServiceEntity.setLastStartedAt(OffsetDateTime.now());
        }
        if (state == DeployerTaskStatus.DEPLOY_FAILED
                || state == DeployerTaskStatus.DESTROY_SUCCESS
                || state == DeployerTaskStatus.PURGE_SUCCESS) {
            deployServiceEntity.setServiceState(ServiceState.NOT_RUNNING);
        }
        // case other cases, do not change the state of service.
    }

    private ServiceDeploymentState getServiceDeploymentState(
            DeployerTaskStatus deployerTaskStatus) {
        return switch (deployerTaskStatus) {
            case DEPLOY_SUCCESS -> ServiceDeploymentState.DEPLOY_SUCCESS;
            case DEPLOY_FAILED, ROLLBACK_SUCCESS -> ServiceDeploymentState.DEPLOY_FAILED;
            case DESTROY_SUCCESS, PURGE_SUCCESS -> ServiceDeploymentState.DESTROY_SUCCESS;
            case DESTROY_FAILED -> ServiceDeploymentState.DESTROY_FAILED;
            case ROLLBACK_FAILED -> ServiceDeploymentState.ROLLBACK_FAILED;
            case MODIFICATION_SUCCESSFUL -> ServiceDeploymentState.MODIFICATION_SUCCESSFUL;
            case MODIFICATION_FAILED -> ServiceDeploymentState.MODIFICATION_FAILED;
            case INIT, PURGE_FAILED -> ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED;
        };
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
}
