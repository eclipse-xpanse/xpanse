/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.servicelock;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.deployment.ServiceDeploymentEntityHandler;
import org.eclipse.xpanse.modules.deployment.ServiceOrderManager;
import org.eclipse.xpanse.modules.models.common.enums.UserOperation;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.enums.Handler;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/** Implementation class of ServiceLockConfigService. */
@Slf4j
@Component
public class ServiceLockConfigService {

    @Resource private ServiceOrderManager serviceOrderManager;

    @Resource private ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;

    @Resource private ServiceOrderStorage serviceOrderStorage;

    @Resource private UserServiceHelper userServiceHelper;

    /**
     * Method to change lock config of service.
     *
     * @param serviceId serviceId.
     * @param lockConfig serviceLockConfig.
     */
    public ServiceOrder changeServiceLockConfig(UUID serviceId, ServiceLockConfig lockConfig) {
        ServiceDeploymentEntity deployedService =
                serviceDeploymentEntityHandler.getServiceDeploymentEntity(serviceId);
        boolean currentUserIsOwner =
                userServiceHelper.currentUserIsOwner(deployedService.getUserId());
        if (!currentUserIsOwner) {
            String errorMsg =
                    String.format(
                            "No permission to %s owned by other users.",
                            UserOperation.CHANGE_SERVICE_LOCK_CONFIGURATION.toValue());
            log.error(errorMsg);
            throw new AccessDeniedException(errorMsg);
        }
        ServiceOrderType taskType = ServiceOrderType.LOCK_CHANGE;
        DeployTask lockChangeTask = new DeployTask();
        lockChangeTask.setOrderId(UUID.randomUUID());
        lockChangeTask.setTaskType(taskType);
        lockChangeTask.setServiceId(serviceId);
        lockChangeTask.setUserId(deployedService.getUserId());
        lockChangeTask.setRequest(lockConfig);
        ServiceOrderEntity serviceOrder =
                serviceOrderManager.storeNewServiceOrderEntity(
                        lockChangeTask, deployedService, Handler.INTERNAL);
        serviceOrder.setStartedTime(OffsetDateTime.now());
        deployedService.setLockConfig(lockConfig);
        serviceDeploymentEntityHandler.storeAndFlush(deployedService);
        serviceOrder.setCompletedTime(OffsetDateTime.now());
        serviceOrder.setTaskStatus(TaskStatus.SUCCESSFUL);
        serviceOrderStorage.storeAndFlush(serviceOrder);
        return new ServiceOrder(serviceOrder.getOrderId(), deployedService.getId());
    }
}
