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
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.DeployerTaskStatus;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceState;
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
    private DeployServiceStorage deployServiceStorage;

    @Resource
    private ServiceOrderStorage serviceOrderStorage;

    @Resource
    private SensitiveDataHandler sensitiveDataHandler;

    @Resource
    private DeployServiceEntityConverter deployServiceEntityConverter;

    /**
     * Method to update deployment result to DeployServiceEntity and store to database.
     *
     * @param deployResult Deployment Result.
     * @param storedEntity DB entity to be updated
     * @return updated entity.
     * @throws RuntimeException exception thrown in case of errors.
     */
    public DeployServiceEntity updateDeployServiceEntityWithDeployResult(
            DeployResult deployResult, DeployServiceEntity storedEntity) {
        if (Objects.isNull(deployResult) || Objects.isNull(deployResult.getState())) {
            return storedEntity;
        }
        if (Objects.isNull(storedEntity)) {
            storedEntity = deployServiceStorage.findDeployServiceById(deployResult.getServiceId());
        }
        log.info("Update deploy service entity {} with deploy result {}",
                deployResult.getServiceId(), deployResult);
        DeployServiceEntity deployServiceToUpdate = new DeployServiceEntity();
        BeanUtils.copyProperties(storedEntity, deployServiceToUpdate);
        updateServiceEntityWithDeployResult(deployResult, deployServiceToUpdate);
        return deployServiceStorage.storeAndFlush(deployServiceToUpdate);
    }

    private void updateServiceEntityWithDeployResult(DeployResult deployResult,
                                                     DeployServiceEntity deployServiceEntity) {
        if (StringUtils.isNotBlank(deployResult.getMessage())) {
            deployServiceEntity.setResultMessage(deployResult.getMessage());
        } else {
            // When rollback successfully, the result message should be the previous error message.
            if (deployResult.getState() != DeployerTaskStatus.ROLLBACK_SUCCESS) {
                deployServiceEntity.setResultMessage(null);
            }
        }
        if (deployResult.getState() == DeployerTaskStatus.MODIFICATION_SUCCESSFUL) {
            DeployRequest modifyRequest = deployServiceEntity.getDeployRequest();
            deployServiceEntity.setFlavor(modifyRequest.getFlavor());
            deployServiceEntity.setCustomerServiceName(modifyRequest.getCustomerServiceName());
        }
        deployServiceEntity.setServiceDeploymentState(
                getServiceDeploymentState(deployResult.getState()));

        DeployerTaskStatus deployerTaskStatus = deployResult.getState();
        updateServiceConfiguration(deployerTaskStatus, deployServiceEntity);
        updateServiceState(deployerTaskStatus, deployServiceEntity);

        boolean isTaskSuccessful = deployResult.getIsTaskSuccessful();
        if (CollectionUtils.isEmpty(deployResult.getPrivateProperties())) {
            if (isTaskSuccessful) {
                deployServiceEntity.getPrivateProperties().clear();
            }
        } else {
            deployServiceEntity.setPrivateProperties(deployResult.getPrivateProperties());
        }

        if (CollectionUtils.isEmpty(deployResult.getProperties())) {
            if (isTaskSuccessful) {
                deployServiceEntity.getProperties().clear();
            }
        } else {
            deployServiceEntity.setProperties(deployResult.getProperties());
        }

        if (CollectionUtils.isEmpty(deployResult.getResources())) {
            if (isTaskSuccessful) {
                deployServiceEntity.getDeployResourceList().clear();
            }
        } else {
            deployServiceEntity.setDeployResourceList(
                    getDeployResourceEntityList(deployResult.getResources(), deployServiceEntity));
        }
        sensitiveDataHandler.maskSensitiveFields(deployServiceEntity);
    }

    private void updateServiceConfiguration(DeployerTaskStatus state,
                                            DeployServiceEntity deployServiceEntity) {
        if (state == DeployerTaskStatus.DEPLOY_SUCCESS) {
            ServiceConfigurationEntity serviceConfigurationEntity =
                    deployServiceEntityConverter.getInitialServiceConfiguration(
                            deployServiceEntity);
            deployServiceEntity.setServiceConfigurationEntity(serviceConfigurationEntity);
        }
        if (state == DeployerTaskStatus.DESTROY_SUCCESS) {
            deployServiceEntity.setServiceConfigurationEntity(null);
        }
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

    /**
     * Update service order task in the database by the deployment result.
     *
     * @param deployResult Deployment Result.
     * @param storedEntity DB entity to be updated
     */
    public void updateServiceOrderTaskWithDeployResult(DeployResult deployResult,
                                                       ServiceOrderEntity storedEntity) {
        if (Objects.isNull(deployResult) || Objects.isNull(deployResult.getIsTaskSuccessful())) {
            return;
        }
        if (Objects.isNull(storedEntity)) {
            storedEntity = serviceOrderStorage.getEntityById(deployResult.getOrderId());
        }
        ServiceOrderEntity entityToUpdate = new ServiceOrderEntity();
        BeanUtils.copyProperties(storedEntity, entityToUpdate);
        DeployerTaskStatus deployerTaskStatus = deployResult.getState();
        if (deployResult.getIsTaskSuccessful()) {
            // When the status is rollback_success, the deployment order status should be failed.
            if (deployerTaskStatus == DeployerTaskStatus.ROLLBACK_SUCCESS) {
                entityToUpdate.setTaskStatus(TaskStatus.FAILED);
            } else {
                entityToUpdate.setTaskStatus(TaskStatus.SUCCESSFUL);
            }
            entityToUpdate.setCompletedTime(OffsetDateTime.now());
        } else {
            entityToUpdate.setErrorMsg(deployResult.getMessage());
            // When deploy failed, the order is not be completed util the rollback is done.
            if (deployerTaskStatus != DeployerTaskStatus.DEPLOY_FAILED) {
                entityToUpdate.setTaskStatus(TaskStatus.FAILED);
                entityToUpdate.setCompletedTime(OffsetDateTime.now());
            }
        }
        serviceOrderStorage.storeAndFlush(entityToUpdate);
    }
}
