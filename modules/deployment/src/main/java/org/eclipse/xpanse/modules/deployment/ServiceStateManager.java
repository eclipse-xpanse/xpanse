/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import static org.eclipse.xpanse.modules.async.TaskConfiguration.ASYNC_EXECUTOR_NAME;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.resource.ServiceResourceEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceorder.DatabaseServiceOrderStorage;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceState;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Bean to manage service state.
 */
@Component
@Slf4j
public class ServiceStateManager {

    @Resource
    private ServiceDeploymentEntityHandler serviceHandler;
    @Resource
    private PluginManager pluginManager;
    @Resource
    private UserServiceHelper userServiceHelper;
    @Resource
    private DatabaseServiceOrderStorage serviceOrderStorage;
    @Resource(name = ASYNC_EXECUTOR_NAME)
    private Executor taskExecutor;
    @Resource
    private ServiceOrderManager serviceOrderManager;

    /**
     * Start the service by the deployed service id.
     *
     * @param id service id.
     * @return task id.
     */
    public ServiceOrder startService(UUID id) {
        ServiceOrderType taskType = ServiceOrderType.SERVICE_START;
        UUID orderId = UUID.randomUUID();
        ServiceDeploymentEntity service = getDeployedServiceAndValidateState(id, orderId, taskType);
        OrchestratorPlugin plugin = pluginManager.getOrchestratorPlugin(service.getCsp());
        ServiceStateManageRequest startRequest = getServiceManagerRequest(service, orderId);
        ServiceOrderEntity serviceOrderEntity = createNewManagementTask(orderId, taskType, service);
        taskExecutor.execute(
                () -> asyncStartService(serviceOrderEntity, plugin, startRequest, service));
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setServiceId(id);
        serviceOrder.setOrderId(serviceOrderEntity.getOrderId());
        return serviceOrder;
    }

    private ServiceOrderEntity createNewManagementTask(
            UUID orderId, ServiceOrderType taskType, ServiceDeploymentEntity service) {
        DeployTask deployTask = new DeployTask();
        deployTask.setOrderId(orderId);
        deployTask.setServiceId(service.getId());
        deployTask.setTaskType(taskType);
        deployTask.setUserId(getUserId());
        return serviceOrderManager.storeNewServiceOrderEntity(deployTask, service);
    }

    private void asyncStartService(ServiceOrderEntity serviceOrderTaskEntity,
                                   OrchestratorPlugin plugin, ServiceStateManageRequest request,
                                   ServiceDeploymentEntity service) {
        serviceOrderTaskEntity = serviceOrderManager.startOrderProgress(serviceOrderTaskEntity);
        service.setServiceState(ServiceState.STARTING);
        serviceHandler.storeAndFlush(service);
        boolean result = false;
        try {
            result = plugin.startService(request);
        } catch (Exception e) {
            serviceOrderTaskEntity.setErrorMsg(e.getMessage());
        }
        serviceOrderTaskEntity.setCompletedTime(OffsetDateTime.now());
        if (result) {
            serviceOrderTaskEntity.setTaskStatus(TaskStatus.SUCCESSFUL);
            service.setLastStartedAt(OffsetDateTime.now());
            service.setServiceState(ServiceState.RUNNING);
        } else {
            serviceOrderTaskEntity.setTaskStatus(TaskStatus.FAILED);
            service.setServiceState(ServiceState.STOPPED);
        }
        serviceOrderStorage.storeAndFlush(serviceOrderTaskEntity);
        serviceHandler.storeAndFlush(service);
    }

    /**
     * Stop the service by the deployed service id.
     *
     * @param id service id.
     * @return task id.
     */
    public ServiceOrder stopService(UUID id) {
        ServiceOrderType taskType = ServiceOrderType.SERVICE_STOP;
        UUID orderId = UUID.randomUUID();
        ServiceDeploymentEntity service = getDeployedServiceAndValidateState(id, orderId, taskType);
        OrchestratorPlugin plugin = pluginManager.getOrchestratorPlugin(service.getCsp());
        ServiceStateManageRequest stopRequest = getServiceManagerRequest(service, orderId);
        ServiceOrderEntity serviceOrderEntity = createNewManagementTask(orderId, taskType, service);
        taskExecutor.execute(
                () -> asyncStopService(serviceOrderEntity, plugin, stopRequest, service));
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setServiceId(id);
        serviceOrder.setOrderId(serviceOrderEntity.getOrderId());
        return serviceOrder;
    }

    private void asyncStopService(ServiceOrderEntity serviceOrderTaskEntity,
                                  OrchestratorPlugin plugin, ServiceStateManageRequest request,
                                  ServiceDeploymentEntity service) {
        serviceOrderTaskEntity.setTaskStatus(TaskStatus.IN_PROGRESS);
        serviceOrderTaskEntity = serviceOrderManager.startOrderProgress(serviceOrderTaskEntity);
        service.setServiceState(ServiceState.STOPPING);
        serviceHandler.storeAndFlush(service);
        boolean result = false;
        try {
            result = plugin.stopService(request);
        } catch (Exception e) {
            serviceOrderTaskEntity.setErrorMsg(e.getMessage());
        }
        serviceOrderTaskEntity.setCompletedTime(OffsetDateTime.now());
        if (result) {
            serviceOrderTaskEntity.setTaskStatus(TaskStatus.SUCCESSFUL);
            service.setLastStoppedAt(OffsetDateTime.now());
            service.setServiceState(ServiceState.STOPPED);
        } else {
            serviceOrderTaskEntity.setTaskStatus(TaskStatus.FAILED);
            service.setServiceState(ServiceState.RUNNING);
        }
        serviceOrderStorage.storeAndFlush(serviceOrderTaskEntity);
        serviceHandler.storeAndFlush(service);
    }

    /**
     * Restart the service by the deployed service id.
     *
     * @param id service id.
     * @return task id.
     */
    public ServiceOrder restartService(UUID id) {
        ServiceOrderType taskType = ServiceOrderType.SERVICE_RESTART;
        UUID orderId = UUID.randomUUID();
        ServiceDeploymentEntity service = getDeployedServiceAndValidateState(id, orderId, taskType);
        OrchestratorPlugin plugin = pluginManager.getOrchestratorPlugin(service.getCsp());
        ServiceStateManageRequest restartRequest = getServiceManagerRequest(service, orderId);
        ServiceOrderEntity serviceOrderEntity = createNewManagementTask(orderId, taskType, service);
        taskExecutor.execute(
                () -> asyncRestartService(serviceOrderEntity, plugin, restartRequest, service));
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setServiceId(id);
        serviceOrder.setOrderId(serviceOrderEntity.getOrderId());
        return serviceOrder;
    }

    private void asyncRestartService(ServiceOrderEntity serviceOrderTaskEntity,
                                     OrchestratorPlugin plugin, ServiceStateManageRequest request,
                                     ServiceDeploymentEntity service) {
        serviceOrderTaskEntity = serviceOrderManager.startOrderProgress(serviceOrderTaskEntity);
        service.setServiceState(ServiceState.RESTARTING);
        serviceHandler.storeAndFlush(service);
        boolean result = false;
        try {
            result = plugin.restartService(request);
        } catch (Exception e) {
            serviceOrderTaskEntity.setErrorMsg(e.getMessage());
        }
        serviceOrderTaskEntity.setCompletedTime(OffsetDateTime.now());
        if (result) {
            serviceOrderTaskEntity.setTaskStatus(TaskStatus.SUCCESSFUL);
            service.setLastStartedAt(OffsetDateTime.now());
            service.setServiceState(ServiceState.RUNNING);
        } else {
            serviceOrderTaskEntity.setTaskStatus(TaskStatus.FAILED);
            service.setServiceState(ServiceState.RUNNING);
        }
        serviceOrderStorage.storeAndFlush(serviceOrderTaskEntity);
        serviceHandler.storeAndFlush(service);
    }

    private ServiceDeploymentEntity getDeployedServiceAndValidateState(
            UUID serviceId, UUID orderId, ServiceOrderType taskType) {
        ServiceDeploymentEntity service = serviceHandler.getServiceDeploymentEntity(serviceId);
        validateDeployServiceEntity(service, orderId);
        if (service.getServiceState() == ServiceState.STARTING
                || service.getServiceState() == ServiceState.STOPPING
                || service.getServiceState() == ServiceState.RESTARTING) {
            throw new InvalidServiceStateException(String.format(
                    "Task %s: Service %s with a running management task, please try again later.",
                    orderId, serviceId));
        }
        if (taskType == ServiceOrderType.SERVICE_START) {
            validateStartActionForService(service, orderId);
        } else if (taskType == ServiceOrderType.SERVICE_STOP) {
            validateStopActionForService(service, orderId);
        } else if (taskType == ServiceOrderType.SERVICE_RESTART) {
            validateRestartActionForService(service, orderId);
        }
        return service;
    }

    private void validateStartActionForService(ServiceDeploymentEntity service, UUID orderId) {
        if (!(service.getServiceState() == ServiceState.STOPPED
                || service.getServiceState() == ServiceState.NOT_RUNNING)) {
            throw new InvalidServiceStateException(
                    String.format("Task %s: Service %s with state %s is not supported to start.",
                            orderId, service.getId(), service.getServiceState()));
        }
    }

    private void validateStopActionForService(ServiceDeploymentEntity service, UUID orderId) {
        if (service.getServiceState() != ServiceState.RUNNING) {
            throw new InvalidServiceStateException(
                    String.format("Task %s: Service %s with state %s is not supported to stop.",
                            orderId, service.getId(), service.getServiceState()));
        }
    }

    private void validateRestartActionForService(ServiceDeploymentEntity service, UUID orderId) {
        if (service.getServiceState() != ServiceState.RUNNING) {
            throw new InvalidServiceStateException(
                    String.format("Task %s: Service %s with state %s is not supported to restart.",
                            orderId, service.getId(), service.getServiceState()));
        }
    }

    private void validateDeployServiceEntity(ServiceDeploymentEntity service, UUID orderId) {
        if (service.getDeployRequest().getServiceHostingType() == ServiceHostingType.SELF) {
            boolean currentUserIsOwner = userServiceHelper.currentUserIsOwner(service.getUserId());
            if (!currentUserIsOwner) {
                throw new AccessDeniedException(
                        String.format(
                                "Task %s: No permissions to manage status of the service "
                                        + "belonging to "
                                        + "other users.",
                                orderId));
            }
        }
        ServiceDeploymentState serviceDeploymentState = service.getServiceDeploymentState();
        if (!(serviceDeploymentState == ServiceDeploymentState.DEPLOY_SUCCESS
                || serviceDeploymentState == ServiceDeploymentState.DESTROY_FAILED
                || serviceDeploymentState == ServiceDeploymentState.MODIFICATION_SUCCESSFUL
                || serviceDeploymentState == ServiceDeploymentState.MODIFICATION_FAILED)) {
            String errorMsg =
                    String.format("Task %s: Service %s with deployment state %s is not supported"
                                    + " to manage status.", orderId, service.getId(),
                            serviceDeploymentState);
            log.error(errorMsg);
            throw new InvalidServiceStateException(errorMsg);
        }
    }

    private ServiceStateManageRequest getServiceManagerRequest(ServiceDeploymentEntity service,
                                                               UUID orderId) {
        ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setServiceId(service.getId());
        List<ServiceResourceEntity> vmResources =
                CollectionUtils.isEmpty(service.getDeployResourceList()) ? Collections.emptyList()
                        : service.getDeployResourceList().stream()
                        .filter(resource -> resource.getResourceKind() == DeployResourceKind.VM)
                        .toList();
        if (CollectionUtils.isEmpty(vmResources)) {
            String errorMsg =
                    String.format("Task %s: Service with id %s has no vm resources.", orderId,
                            service.getId());
            log.error(errorMsg);
            throw new ServiceNotDeployedException(errorMsg);
        }
        serviceStateManageRequest.setServiceResourceEntityList(vmResources);
        ServiceHostingType serviceHostingType = service.getDeployRequest().getServiceHostingType();
        if (serviceHostingType == ServiceHostingType.SELF) {
            serviceStateManageRequest.setUserId(service.getUserId());
        }
        serviceStateManageRequest.setRegion(service.getDeployRequest().getRegion());
        return serviceStateManageRequest;
    }

    private String getUserId() {
        return userServiceHelper.getCurrentUserId();
    }
}
