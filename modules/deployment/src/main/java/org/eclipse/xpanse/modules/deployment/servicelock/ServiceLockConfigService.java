/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.servicelock;

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
import org.eclipse.xpanse.modules.models.service.enums.OrderStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/** Implementation class of ServiceLockConfigService. */
@Slf4j
@Component
public class ServiceLockConfigService {

    private final ServiceOrderManager serviceOrderManager;
    private final ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;
    private final ServiceOrderStorage serviceOrderStorage;
    private final UserServiceHelper userServiceHelper;

    /** Constructor method. */
    @Autowired
    public ServiceLockConfigService(
            ServiceOrderManager serviceOrderManager,
            ServiceDeploymentEntityHandler serviceDeploymentEntityHandler,
            ServiceOrderStorage serviceOrderStorage,
            UserServiceHelper userServiceHelper) {
        this.serviceOrderManager = serviceOrderManager;
        this.serviceDeploymentEntityHandler = serviceDeploymentEntityHandler;
        this.serviceOrderStorage = serviceOrderStorage;
        this.userServiceHelper = userServiceHelper;
    }

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
        serviceOrder.setOrderStatus(OrderStatus.SUCCESSFUL);
        serviceOrderStorage.storeAndFlush(serviceOrder);
        return new ServiceOrder(serviceOrder.getOrderId(), deployedService.getId());
    }
}
