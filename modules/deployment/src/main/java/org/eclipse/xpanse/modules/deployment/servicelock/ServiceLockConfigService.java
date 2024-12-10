/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.servicelock;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityConverter;
import org.eclipse.xpanse.modules.deployment.ServiceDeploymentEntityHandler;
import org.eclipse.xpanse.modules.deployment.ServiceOrderManager;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.enums.Handler;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;


/**
 * Implementation class of ServiceLockConfigService.
 */
@Component
public class ServiceLockConfigService {

    @Resource
    private ServiceOrderManager serviceOrderManager;

    @Resource
    private ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;

    @Resource
    private DeployServiceEntityConverter deployServiceEntityConverter;

    @Resource
    private ServiceOrderStorage serviceOrderStorage;

    @Resource
    private UserServiceHelper userServiceHelper;

    /**
     * Method to change lock config of service.
     *
     * @param serviceId  serviceId.
     * @param lockConfig serviceLockConfig.
     */
    public ServiceOrder changeServiceLockConfig(UUID serviceId,
                                                ServiceLockConfig lockConfig) {
        ServiceDeploymentEntity deployedService =
                serviceDeploymentEntityHandler.getServiceDeploymentEntity(serviceId);
        boolean currentUserIsOwner =
                userServiceHelper.currentUserIsOwner(deployedService.getUserId());
        if (!currentUserIsOwner) {
            String errorMsg = "No permissions to change lock config of services "
                    + "belonging to other users.";
            throw new AccessDeniedException(errorMsg);
        }

        DeployTask lockChangeTask = deployServiceEntityConverter.getDeployTaskByStoredService(
                ServiceOrderType.LOCK_CHANGE, deployedService);
        lockChangeTask.setRequest(lockConfig);
        ServiceOrderEntity serviceOrder = serviceOrderManager
                .storeNewServiceOrderEntity(lockChangeTask, deployedService, Handler.INTERNAL);
        serviceOrder.setStartedTime(OffsetDateTime.now());
        deployedService.setLockConfig(lockConfig);
        serviceDeploymentEntityHandler.storeAndFlush(deployedService);
        serviceOrder.setCompletedTime(OffsetDateTime.now());
        serviceOrder.setTaskStatus(TaskStatus.SUCCESSFUL);
        serviceOrderStorage.storeAndFlush(serviceOrder);
        return new ServiceOrder(serviceOrder.getOrderId(), deployedService.getId());
    }
}
