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
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.resource.ServiceResourceEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.models.common.enums.UserOperation;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceLockedException;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.enums.Handler;
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
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Bean to manage service state. */
@Component
@Slf4j
public class ServiceStateManager {

    @Resource private ServiceDeploymentEntityHandler serviceHandler;
    @Resource private PluginManager pluginManager;
    @Resource private UserServiceHelper userServiceHelper;
    @Resource private ServiceOrderManager serviceOrderManager;

    @Resource(name = ASYNC_EXECUTOR_NAME)
    private Executor taskExecutor;

    /**
     * Start the service by the deployed service id.
     *
     * @param serviceId service id.
     * @return service order.
     */
    public ServiceOrder startService(UUID serviceId) {
        ServiceOrderType taskType = ServiceOrderType.SERVICE_START;
        ServiceDeploymentEntity service = getDeployedServiceAndValidateState(serviceId, taskType);
        OrchestratorPlugin plugin = pluginManager.getOrchestratorPlugin(service.getCsp());
        ServiceStateManageRequest startRequest = getServiceManagerRequest(service);
        ServiceOrderEntity serviceOrderEntity = createNewManagementTask(taskType, service);
        taskExecutor.execute(
                () -> asyncStartService(serviceOrderEntity, plugin, startRequest, service));
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setServiceId(serviceId);
        serviceOrder.setOrderId(serviceOrderEntity.getOrderId());
        return serviceOrder;
    }

    private ServiceOrderEntity createNewManagementTask(
            ServiceOrderType taskType, ServiceDeploymentEntity service) {
        DeployTask deployTask = new DeployTask();
        deployTask.setOrderId(UUID.randomUUID());
        deployTask.setServiceId(service.getId());
        deployTask.setTaskType(taskType);
        deployTask.setUserId(getUserId());
        return serviceOrderManager.storeNewServiceOrderEntity(deployTask, service, Handler.PLUGIN);
    }

    private void asyncStartService(
            ServiceOrderEntity serviceOrderTaskEntity,
            OrchestratorPlugin plugin,
            ServiceStateManageRequest request,
            ServiceDeploymentEntity service) {
        serviceOrderTaskEntity = serviceOrderManager.startOrderProgress(serviceOrderTaskEntity);
        service.setServiceState(ServiceState.STARTING);
        serviceHandler.storeAndFlush(service);
        boolean result = false;
        try {
            result = plugin.startService(request);
        } catch (Exception e) {
            serviceOrderTaskEntity.setErrorResponse(
                    ErrorResponse.errorResponse(
                            ErrorType.ASYNC_START_SERVICE_ERROR, List.of(e.getMessage())));
        }
        if (result) {
            serviceOrderTaskEntity.setTaskStatus(TaskStatus.SUCCESSFUL);
            service.setLastStartedAt(OffsetDateTime.now());
            service.setServiceState(ServiceState.RUNNING);
        } else {
            serviceOrderTaskEntity.setTaskStatus(TaskStatus.FAILED);
            service.setServiceState(ServiceState.STOPPED);
        }
        serviceOrderManager.completeOrderProgress(
                serviceOrderTaskEntity.getOrderId(),
                serviceOrderTaskEntity.getTaskStatus(),
                serviceOrderTaskEntity.getErrorResponse());
        serviceHandler.storeAndFlush(service);
    }

    /**
     * Stop the service by the deployed service id.
     *
     * @param serviceId service id.
     * @return service order.
     */
    public ServiceOrder stopService(UUID serviceId) {
        ServiceOrderType taskType = ServiceOrderType.SERVICE_STOP;
        ServiceDeploymentEntity service = getDeployedServiceAndValidateState(serviceId, taskType);
        OrchestratorPlugin plugin = pluginManager.getOrchestratorPlugin(service.getCsp());
        ServiceStateManageRequest stopRequest = getServiceManagerRequest(service);
        ServiceOrderEntity serviceOrderEntity = createNewManagementTask(taskType, service);
        taskExecutor.execute(
                () -> asyncStopService(serviceOrderEntity, plugin, stopRequest, service));
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setServiceId(serviceId);
        serviceOrder.setOrderId(serviceOrderEntity.getOrderId());
        return serviceOrder;
    }

    private void asyncStopService(
            ServiceOrderEntity serviceOrderTaskEntity,
            OrchestratorPlugin plugin,
            ServiceStateManageRequest request,
            ServiceDeploymentEntity service) {
        serviceOrderTaskEntity.setTaskStatus(TaskStatus.IN_PROGRESS);
        serviceOrderTaskEntity = serviceOrderManager.startOrderProgress(serviceOrderTaskEntity);
        service.setServiceState(ServiceState.STOPPING);
        serviceHandler.storeAndFlush(service);
        boolean result = false;
        try {
            result = plugin.stopService(request);
        } catch (Exception e) {
            serviceOrderTaskEntity.setErrorResponse(
                    ErrorResponse.errorResponse(
                            ErrorType.ASYNC_STOP_SERVICE_ERROR, List.of(e.getMessage())));
        }
        if (result) {
            serviceOrderTaskEntity.setTaskStatus(TaskStatus.SUCCESSFUL);
            service.setLastStoppedAt(OffsetDateTime.now());
            service.setServiceState(ServiceState.STOPPED);
        } else {
            serviceOrderTaskEntity.setTaskStatus(TaskStatus.FAILED);
            service.setServiceState(ServiceState.RUNNING);
        }
        serviceOrderManager.completeOrderProgress(
                serviceOrderTaskEntity.getOrderId(),
                serviceOrderTaskEntity.getTaskStatus(),
                serviceOrderTaskEntity.getErrorResponse());
        serviceHandler.storeAndFlush(service);
    }

    /**
     * Restart the service by the deployed service id.
     *
     * @param serviceId service id.
     * @return service order.
     */
    public ServiceOrder restartService(UUID serviceId) {
        ServiceOrderType taskType = ServiceOrderType.SERVICE_RESTART;
        ServiceDeploymentEntity service = getDeployedServiceAndValidateState(serviceId, taskType);
        OrchestratorPlugin plugin = pluginManager.getOrchestratorPlugin(service.getCsp());
        ServiceStateManageRequest restartRequest = getServiceManagerRequest(service);
        ServiceOrderEntity serviceOrderEntity = createNewManagementTask(taskType, service);
        taskExecutor.execute(
                () -> asyncRestartService(serviceOrderEntity, plugin, restartRequest, service));
        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setServiceId(serviceId);
        serviceOrder.setOrderId(serviceOrderEntity.getOrderId());
        return serviceOrder;
    }

    private void asyncRestartService(
            ServiceOrderEntity serviceOrderTaskEntity,
            OrchestratorPlugin plugin,
            ServiceStateManageRequest request,
            ServiceDeploymentEntity service) {
        serviceOrderTaskEntity = serviceOrderManager.startOrderProgress(serviceOrderTaskEntity);
        service.setServiceState(ServiceState.RESTARTING);
        serviceHandler.storeAndFlush(service);
        boolean result = false;
        try {
            result = plugin.restartService(request);
        } catch (Exception e) {
            serviceOrderTaskEntity.setErrorResponse(
                    ErrorResponse.errorResponse(
                            ErrorType.ASYNC_RESTART_SERVICE_ERROR, List.of(e.getMessage())));
        }
        if (result) {
            serviceOrderTaskEntity.setTaskStatus(TaskStatus.SUCCESSFUL);
            service.setLastStartedAt(OffsetDateTime.now());
            service.setServiceState(ServiceState.RUNNING);
        } else {
            serviceOrderTaskEntity.setTaskStatus(TaskStatus.FAILED);
            service.setServiceState(ServiceState.RUNNING);
        }
        serviceOrderManager.completeOrderProgress(
                serviceOrderTaskEntity.getOrderId(),
                serviceOrderTaskEntity.getTaskStatus(),
                serviceOrderTaskEntity.getErrorResponse());
        serviceHandler.storeAndFlush(service);
    }

    private ServiceDeploymentEntity getDeployedServiceAndValidateState(
            UUID serviceId, ServiceOrderType taskType) {
        ServiceDeploymentEntity service = serviceHandler.getServiceDeploymentEntity(serviceId);
        validateDeployServiceEntity(service);
        if (service.getServiceState() == ServiceState.STARTING
                || service.getServiceState() == ServiceState.STOPPING
                || service.getServiceState() == ServiceState.RESTARTING) {
            throw new InvalidServiceStateException(
                    String.format(
                            "Service %s with a running management task, please try again"
                                    + " later.",
                            serviceId));
        }
        if (taskType == ServiceOrderType.SERVICE_START) {
            validateStartActionForService(service);
        } else if (taskType == ServiceOrderType.SERVICE_STOP) {
            validateStopActionForService(service);
        } else if (taskType == ServiceOrderType.SERVICE_RESTART) {
            validateRestartActionForService(service);
        }
        return service;
    }

    private void validateStartActionForService(ServiceDeploymentEntity service) {
        if (!(service.getServiceState() == ServiceState.STOPPED
                || service.getServiceState() == ServiceState.NOT_RUNNING)) {
            String errorMsg =
                    String.format(
                            "Service %s with state %s is not supported to start.",
                            service.getId(), service.getServiceState().toValue());
            log.error(errorMsg);
            throw new InvalidServiceStateException(errorMsg);
        }
    }

    private void validateStopActionForService(ServiceDeploymentEntity service) {
        if (Objects.nonNull(service.getLockConfig()) && service.getLockConfig().isModifyLocked()) {
            String errorMsg =
                    String.format("Service with id %s is locked from stop.", service.getId());
            log.error(errorMsg);
            throw new ServiceLockedException(errorMsg);
        }

        if (service.getServiceState() != ServiceState.RUNNING) {
            String errorMsg =
                    String.format(
                            "Service %s with state %s is not supported to stop.",
                            service.getId(), service.getServiceState().toValue());
            log.error(errorMsg);
            throw new InvalidServiceStateException(errorMsg);
        }
    }

    private void validateRestartActionForService(ServiceDeploymentEntity service) {
        if (Objects.nonNull(service.getLockConfig()) && service.getLockConfig().isModifyLocked()) {
            String errorMsg =
                    String.format("Service with id %s is locked from restart.", service.getId());
            log.error(errorMsg);
            throw new ServiceLockedException(errorMsg);
        }

        if (service.getServiceState() != ServiceState.RUNNING) {
            String errorMsg =
                    String.format(
                            "Service %s with state %s is not supported to restart.",
                            service.getId(), service.getServiceState().toValue());
            log.error(errorMsg);
            throw new InvalidServiceStateException(errorMsg);
        }
    }

    private void validateDeployServiceEntity(ServiceDeploymentEntity service) {
        if (service.getServiceHostingType() == ServiceHostingType.SELF) {
            boolean currentUserIsOwner = userServiceHelper.currentUserIsOwner(service.getUserId());
            if (!currentUserIsOwner) {
                String errorMsg =
                        String.format(
                                "No permission to %s owned by other users.",
                                UserOperation.CHANGE_SERVICE_STATE.toValue());
                log.error(errorMsg);
                throw new AccessDeniedException(errorMsg);
            }
        }
        ServiceDeploymentState serviceDeploymentState = service.getServiceDeploymentState();
        if (!(serviceDeploymentState == ServiceDeploymentState.DEPLOY_SUCCESS
                || serviceDeploymentState == ServiceDeploymentState.DESTROY_FAILED
                || serviceDeploymentState == ServiceDeploymentState.MODIFICATION_SUCCESSFUL
                || serviceDeploymentState == ServiceDeploymentState.MODIFICATION_FAILED)) {
            String errorMsg =
                    String.format(
                            "Service %s with deployment state %s is not supported"
                                    + " to manage power state.",
                            service.getId(), serviceDeploymentState.toValue());
            log.error(errorMsg);
            throw new InvalidServiceStateException(errorMsg);
        }
    }

    private ServiceStateManageRequest getServiceManagerRequest(ServiceDeploymentEntity service) {
        ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setServiceId(service.getId());
        List<ServiceResourceEntity> vmResources =
                CollectionUtils.isEmpty(service.getDeployResources())
                        ? Collections.emptyList()
                        : service.getDeployResources().stream()
                                .filter(
                                        resource ->
                                                resource.getResourceKind() == DeployResourceKind.VM)
                                .toList();
        if (CollectionUtils.isEmpty(vmResources)) {
            String errorMsg =
                    String.format("Service with id %s has no vm resources.", service.getId());
            log.error(errorMsg);
            throw new ServiceNotDeployedException(errorMsg);
        }
        serviceStateManageRequest.setServiceResourceEntityList(vmResources);
        ServiceHostingType serviceHostingType = service.getServiceHostingType();
        if (serviceHostingType == ServiceHostingType.SELF) {
            serviceStateManageRequest.setUserId(service.getUserId());
        }
        serviceStateManageRequest.setRegion(service.getRegion());
        return serviceStateManageRequest;
    }

    private String getUserId() {
        return userServiceHelper.getCurrentUserId();
    }
}
