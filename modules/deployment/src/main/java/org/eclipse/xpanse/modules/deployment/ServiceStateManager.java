/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ADMIN;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DatabaseDeployServiceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.servicestatemanagement.DatabaseServiceStateManagementTaskStorage;
import org.eclipse.xpanse.modules.database.servicestatemanagement.ServiceStateManagementTaskEntity;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.statemanagement.ServiceStateManagementTaskDetails;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceState;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceStateManagementTaskType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private DeployServiceEntityHandler serviceHandler;
    @Resource
    private PluginManager pluginManager;
    @Resource
    private UserServiceHelper userServiceHelper;
    @Resource
    private DatabaseServiceStateManagementTaskStorage taskStorage;
    @Resource
    private DatabaseDeployServiceStorage deployServiceStorage;
    @Qualifier("xpanseAsyncTaskExecutor")
    @Resource
    private Executor taskExecutor;

    /**
     * Start the service by the deployed service id.
     *
     * @param id service id.
     * @return task id.
     */
    public UUID startService(UUID id) {
        ServiceStateManagementTaskType taskType = ServiceStateManagementTaskType.START;
        DeployServiceEntity service = getDeployedServiceAndValidateState(id, taskType);
        OrchestratorPlugin plugin = pluginManager.getOrchestratorPlugin(service.getCsp());
        ServiceStateManageRequest startRequest = getServiceManagerRequest(service);
        ServiceStateManagementTaskEntity newTask = createNewManagementTask(id, taskType);
        taskExecutor.execute(() -> asyncStartService(newTask, plugin, startRequest, service));
        return newTask.getTaskId();
    }

    private ServiceStateManagementTaskEntity createNewManagementTask(
            UUID serviceId, ServiceStateManagementTaskType taskType) {
        ServiceStateManagementTaskEntity newTask = new ServiceStateManagementTaskEntity();
        newTask.setTaskId(UUID.randomUUID());
        newTask.setTaskType(taskType);
        newTask.setServiceId(serviceId);
        newTask.setTaskStatus(TaskStatus.CREATED);
        return taskStorage.storeAndFlush(newTask);
    }

    private void asyncStartService(ServiceStateManagementTaskEntity taskEntity,
                                   OrchestratorPlugin plugin, ServiceStateManageRequest request,
                                   DeployServiceEntity service) {
        taskEntity.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskEntity.setStartedTime(OffsetDateTime.now());
        taskStorage.storeAndFlush(taskEntity);
        service.setServiceState(ServiceState.STARTING);
        serviceHandler.storeAndFlush(service);
        boolean result = false;
        try {
            result = plugin.startService(request);
        } catch (Exception e) {
            taskEntity.setErrorMsg(e.getMessage());
        }
        taskEntity.setCompletedTime(OffsetDateTime.now());
        if (result) {
            taskEntity.setTaskStatus(TaskStatus.SUCCESSFUL);
            service.setLastStartedAt(OffsetDateTime.now());
            service.setServiceState(ServiceState.RUNNING);
        } else {
            taskEntity.setTaskStatus(TaskStatus.FAILED);
            service.setServiceState(ServiceState.STOPPED);
        }
        taskStorage.storeAndFlush(taskEntity);
        serviceHandler.storeAndFlush(service);
    }

    /**
     * Stop the service by the deployed service id.
     *
     * @param id service id.
     * @return task id.
     */
    public UUID stopService(UUID id) {
        ServiceStateManagementTaskType taskType = ServiceStateManagementTaskType.STOP;
        DeployServiceEntity service = getDeployedServiceAndValidateState(id, taskType);
        OrchestratorPlugin plugin = pluginManager.getOrchestratorPlugin(service.getCsp());
        ServiceStateManageRequest stopRequest = getServiceManagerRequest(service);
        ServiceStateManagementTaskEntity newTask = createNewManagementTask(id, taskType);
        taskExecutor.execute(() -> asyncStopService(newTask, plugin, stopRequest, service));
        return newTask.getTaskId();
    }

    private void asyncStopService(ServiceStateManagementTaskEntity taskEntity,
                                  OrchestratorPlugin plugin, ServiceStateManageRequest request,
                                  DeployServiceEntity service) {
        taskEntity.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskEntity.setStartedTime(OffsetDateTime.now());
        taskStorage.storeAndFlush(taskEntity);
        service.setServiceState(ServiceState.STOPPING);
        serviceHandler.storeAndFlush(service);
        boolean result = false;
        try {
            result = plugin.stopService(request);
        } catch (Exception e) {
            taskEntity.setErrorMsg(e.getMessage());
        }
        taskEntity.setCompletedTime(OffsetDateTime.now());
        if (result) {
            taskEntity.setTaskStatus(TaskStatus.SUCCESSFUL);
            service.setLastStoppedAt(OffsetDateTime.now());
            service.setServiceState(ServiceState.STOPPED);
        } else {
            taskEntity.setTaskStatus(TaskStatus.FAILED);
            service.setServiceState(ServiceState.RUNNING);
        }
        taskStorage.storeAndFlush(taskEntity);
        serviceHandler.storeAndFlush(service);
    }

    /**
     * Restart the service by the deployed service id.
     *
     * @param id service id.
     * @return task id.
     */
    public UUID restartService(UUID id) {
        ServiceStateManagementTaskType taskType = ServiceStateManagementTaskType.RESTART;
        DeployServiceEntity service = getDeployedServiceAndValidateState(id, taskType);
        OrchestratorPlugin plugin = pluginManager.getOrchestratorPlugin(service.getCsp());
        ServiceStateManageRequest restartRequest = getServiceManagerRequest(service);
        ServiceStateManagementTaskEntity newTask = createNewManagementTask(id, taskType);
        taskExecutor.execute(() -> asyncRestartService(newTask, plugin, restartRequest, service));
        return newTask.getTaskId();
    }

    private void asyncRestartService(ServiceStateManagementTaskEntity taskEntity,
                                     OrchestratorPlugin plugin, ServiceStateManageRequest request,
                                     DeployServiceEntity service) {
        taskEntity.setTaskStatus(TaskStatus.IN_PROGRESS);
        taskEntity.setStartedTime(OffsetDateTime.now());
        taskStorage.storeAndFlush(taskEntity);
        service.setServiceState(ServiceState.RESTARTING);
        serviceHandler.storeAndFlush(service);
        boolean result = false;
        try {
            result = plugin.restartService(request);
        } catch (Exception e) {
            taskEntity.setErrorMsg(e.getMessage());
        }
        taskEntity.setCompletedTime(OffsetDateTime.now());
        if (result) {
            taskEntity.setTaskStatus(TaskStatus.SUCCESSFUL);
            service.setLastStartedAt(OffsetDateTime.now());
            service.setServiceState(ServiceState.RUNNING);
        } else {
            taskEntity.setTaskStatus(TaskStatus.FAILED);
            service.setServiceState(ServiceState.RUNNING);
        }
        taskStorage.storeAndFlush(taskEntity);
        serviceHandler.storeAndFlush(service);
    }


    /**
     * List the service state management tasks.
     *
     * @param serviceId  service id.
     * @param taskType   task type.
     * @param taskStatus task status.
     * @return list of service state management tasks.
     */
    public List<ServiceStateManagementTaskDetails> listServiceStateManagementTasks(
            UUID serviceId, ServiceStateManagementTaskType taskType, TaskStatus taskStatus) {
        ServiceStateManagementTaskEntity taskQuery = new ServiceStateManagementTaskEntity();
        taskQuery.setServiceId(serviceId);
        taskQuery.setTaskType(taskType);
        taskQuery.setTaskStatus(taskStatus);
        List<ServiceStateManagementTaskEntity> taskEntities = taskStorage.queryTasks(taskQuery);
        return taskEntities.stream().map(EntityTransUtils::transToServiceStateManagementTaskDetails)
                .toList();
    }


    /**
     * Delete the service state management tasks by the service id.
     *
     * @param serviceId service id.
     */
    public void deleteManagementTasksByServiceId(UUID serviceId) {
        ServiceStateManagementTaskEntity taskQuery = new ServiceStateManagementTaskEntity();
        taskQuery.setServiceId(serviceId);
        List<ServiceStateManagementTaskEntity> taskEntities = taskStorage.queryTasks(taskQuery);
        taskStorage.deleteBatch(taskEntities);
    }


    /**
     * Get the service state management task details with the task id.
     *
     * @param taskId task id.
     * @return service state management task details.
     */
    public ServiceStateManagementTaskDetails getManagementTaskDetailsByTaskId(UUID taskId) {
        ServiceStateManagementTaskEntity taskEntity = getManagementTaskEntity(taskId);
        return EntityTransUtils.transToServiceStateManagementTaskDetails(taskEntity);
    }

    /**
     * Get the latest running service state management task details with the service id.
     *
     * @param serviceId service id.
     * @return service state management task details.
     */

    public ServiceStateManagementTaskDetails getLatestRunningManagementTask(UUID serviceId) {
        ServiceStateManagementTaskEntity taskQuery = new ServiceStateManagementTaskEntity();
        taskQuery.setServiceId(serviceId);
        List<ServiceStateManagementTaskEntity> taskEntities = taskStorage.queryTasks(taskQuery);
        if (!CollectionUtils.isEmpty(taskEntities)) {
            return EntityTransUtils.transToServiceStateManagementTaskDetails(
                    taskEntities.getFirst());
        }
        return null;
    }

    /**
     * Delete the service state management task details with the task id.
     *
     * @param taskId task id.
     */
    public void deleteManagementTaskByTaskId(UUID taskId) {
        ServiceStateManagementTaskEntity task = getManagementTaskEntity(taskId);
        taskStorage.delete(task);
    }

    private DeployServiceEntity getDeployedServiceAndValidateState(
            UUID serviceId, ServiceStateManagementTaskType taskType) {
        DeployServiceEntity service = serviceHandler.getDeployServiceEntity(serviceId);
        validateDeployServiceEntity(service);
        if (service.getServiceState() == ServiceState.STARTING
                || service.getServiceState() == ServiceState.STOPPING
                || service.getServiceState() == ServiceState.RESTARTING) {
            throw new InvalidServiceStateException(String.format(
                    "Service %s with a running management task, please try again later.",
                    serviceId));
        }
        if (taskType == ServiceStateManagementTaskType.START) {
            validateStartActionForService(service);
        } else if (taskType == ServiceStateManagementTaskType.STOP) {
            validateStopActionForService(service);
        } else if (taskType == ServiceStateManagementTaskType.RESTART) {
            validateRestartActionForService(service);
        }
        return service;
    }

    private void validateStartActionForService(DeployServiceEntity service) {
        if (!(service.getServiceState() == ServiceState.STOPPED
                || service.getServiceState() == ServiceState.NOT_RUNNING)) {
            throw new InvalidServiceStateException(
                    String.format("Service %s with state %s is not supported to start.",
                            service.getId(), service.getServiceState()));
        }
    }

    private void validateStopActionForService(DeployServiceEntity service) {
        if (service.getServiceState() != ServiceState.RUNNING) {
            throw new InvalidServiceStateException(
                    String.format("Service %s with state %s is not supported to stop.",
                            service.getId(), service.getServiceState()));
        }
    }

    private void validateRestartActionForService(DeployServiceEntity service) {
        if (service.getServiceState() != ServiceState.RUNNING) {
            throw new InvalidServiceStateException(
                    String.format("Service %s with state %s is not supported to restart.",
                            service.getId(), service.getServiceState()));
        }
    }

    private void validateDeployServiceEntity(DeployServiceEntity service) {
        if (service.getDeployRequest().getServiceHostingType() == ServiceHostingType.SELF) {
            boolean currentUserIsOwner = userServiceHelper.currentUserIsOwner(service.getUserId());
            if (!currentUserIsOwner) {
                throw new AccessDeniedException(
                        "No permissions to manage status of the service belonging to other users.");
            }
        }
        ServiceDeploymentState serviceDeploymentState = service.getServiceDeploymentState();
        if (!(serviceDeploymentState == ServiceDeploymentState.DEPLOY_SUCCESS
                || serviceDeploymentState == ServiceDeploymentState.DESTROY_FAILED)) {
            throw new InvalidServiceStateException(
                    String.format("Service with id %s is %s.", service.getId(),
                            serviceDeploymentState));
        }
    }


    private ServiceStateManageRequest getServiceManagerRequest(DeployServiceEntity service) {
        ServiceStateManageRequest serviceStateManageRequest = new ServiceStateManageRequest();
        serviceStateManageRequest.setServiceId(service.getId());
        List<DeployResourceEntity> vmResources =
                CollectionUtils.isEmpty(service.getDeployResourceList()) ? Collections.emptyList()
                        : service.getDeployResourceList().stream()
                        .filter(resource -> resource.getResourceKind() == DeployResourceKind.VM)
                        .toList();
        if (CollectionUtils.isEmpty(vmResources)) {
            String errorMsg =
                    String.format("Service with id %s has no vm resources.", service.getId());
            log.error(errorMsg);
            throw new ServiceNotDeployedException(errorMsg);
        }
        serviceStateManageRequest.setDeployResourceEntityList(vmResources);
        ServiceHostingType serviceHostingType = service.getDeployRequest().getServiceHostingType();
        if (serviceHostingType == ServiceHostingType.SELF) {
            serviceStateManageRequest.setUserId(service.getUserId());
        }
        serviceStateManageRequest.setRegion(service.getDeployRequest().getRegion());
        return serviceStateManageRequest;
    }


    /**
     * Get the deployed service entity with the service id.
     *
     * @param serviceId service id.
     * @return DeployServiceEntity.
     */
    public DeployServiceEntity getDeployServiceEntity(UUID serviceId) {
        DeployServiceEntity deployedService = deployServiceStorage.findDeployServiceById(serviceId);
        if (Objects.nonNull(deployedService)) {
            if (isNotOwnerOrAdminUser(deployedService)) {
                String errorMsg = "No permissions to manage service state management tasks of "
                        + "the services belonging to other users";
                throw new AccessDeniedException(errorMsg);
            }
        }
        return deployedService;
    }

    private ServiceStateManagementTaskEntity getManagementTaskEntity(UUID taskId) {
        ServiceStateManagementTaskEntity task = taskStorage.getTaskById(taskId);
        if (Objects.nonNull(task)) {
            DeployServiceEntity deployedService =
                    deployServiceStorage.findDeployServiceById(task.getServiceId());
            if (Objects.nonNull(deployedService)) {
                if (isNotOwnerOrAdminUser(deployedService)) {
                    String errorMsg = "No permissions to manage service state management tasks of "
                            + "the services belonging to other users";
                    throw new AccessDeniedException(errorMsg);
                }
            }
        }
        return task;
    }


    private boolean isNotOwnerOrAdminUser(DeployServiceEntity deployServiceEntity) {
        boolean isOwner = userServiceHelper.currentUserIsOwner(deployServiceEntity.getUserId());
        boolean isAdmin = userServiceHelper.currentUserHasRole(ROLE_ADMIN);
        return !isOwner && !isAdmin;
    }
}
