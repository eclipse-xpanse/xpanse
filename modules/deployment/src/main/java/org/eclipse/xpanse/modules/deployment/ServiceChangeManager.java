/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.resource.ServiceResourceEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeRequestEntity;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeRequestQueryModel;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeRequestStorage;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.utils.EntityTranslationUtils;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.servicechange.AnsibleHostInfo;
import org.eclipse.xpanse.modules.models.servicechange.ServiceChangeRequest;
import org.eclipse.xpanse.modules.models.servicechange.ServiceChangeResult;
import org.eclipse.xpanse.modules.models.servicechange.enums.ServiceChangeStatus;
import org.eclipse.xpanse.modules.models.servicechange.exceptions.ServiceChangeRequestEntityNotFoundException;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeScript;
import org.hibernate.exception.LockTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Central class to manage all types of service changes such as actions, configuration, objects,
 * etc.
 */
@Component
@Slf4j
public class ServiceChangeManager {

    private static final String IP = "ip";
    private static final String HOSTS = "hosts";

    @Resource private ServiceChangeRequestStorage serviceChangeRequestStorage;
    @Resource private ServiceOrderManager serviceOrderManager;
    @Resource private ServiceConfigurationManager serviceConfigurationManager;
    @Resource private ServiceActionManager serviceActionManager;
    @Resource private ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;

    /** returns the oldest pending request for a specific resource of the service. */
    @Transactional
    public ResponseEntity<ServiceChangeRequest> getPendingServiceChangeRequest(
            UUID serviceId, String resourceName) {
        try {
            ServiceChangeRequestEntity oldestRequest =
                    getOldestPendingServiceChangeRequest(serviceId, resourceName);
            if (Objects.isNull(oldestRequest)) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            }
            List<DeployResource> deployResources =
                    getDeployResources(serviceId, DeployResourceKind.VM);
            if (Objects.isNull(oldestRequest.getResourceName())) {
                validateChangeHandler(
                        serviceId, oldestRequest.getChangeHandler(), resourceName, deployResources);
            }
            updateServiceOrderState(oldestRequest);
            ServiceChangeRequestEntity request =
                    updateServiceChangeRequestState(oldestRequest, resourceName);
            if (Objects.isNull(request)) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(getServiceChangeRequest(request, deployResources));
        } catch (LockTimeoutException e) {
            log.error("Update service configuration update request object timed out", e);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
    }

    private ServiceChangeRequestEntity getOldestPendingServiceChangeRequest(
            UUID serviceId, String resourceName) {
        ServiceChangeRequestQueryModel model =
                new ServiceChangeRequestQueryModel(
                        null, serviceId, null, null, ServiceChangeStatus.PENDING);
        List<ServiceChangeRequestEntity> requests =
                serviceChangeRequestStorage.getServiceChangeRequestEntities(model);
        if (CollectionUtils.isEmpty(requests)) {
            return null;
        }
        Optional<ServiceChangeRequestEntity> oldestRequestOptional =
                requests.stream()
                        .filter(
                                request ->
                                        Objects.isNull(request.getResourceName())
                                                || resourceName.equals(request.getResourceName()))
                        .filter(
                                request ->
                                        request.getServiceOrderEntity() != null
                                                && request.getServiceOrderEntity().getStartedTime()
                                                        != null)
                        .min(
                                Comparator.comparing(
                                        request ->
                                                request.getServiceOrderEntity().getStartedTime()));
        return oldestRequestOptional.orElse(null);
    }

    private ServiceChangeRequestEntity updateServiceChangeRequestState(
            ServiceChangeRequestEntity request, String resourceName) {
        if (Objects.isNull(request.getResourceName())) {
            request.setResourceName(resourceName);
        }
        request.setStatus(ServiceChangeStatus.PROCESSING);
        return serviceChangeRequestStorage.storeAndFlush(request);
    }

    private void updateServiceOrderState(ServiceChangeRequestEntity oldestRequest) {
        ServiceOrderEntity serviceOrderEntity = oldestRequest.getServiceOrderEntity();
        log.debug(
                "updating service order state for service order entity {} status to IN_PROGRESS",
                serviceOrderEntity.getOrderId());
        serviceOrderManager.startOrderProgress(serviceOrderEntity);
    }

    private ServiceChangeRequest getServiceChangeRequest(
            ServiceChangeRequestEntity request, List<DeployResource> deployResources) {
        ServiceChangeRequest serviceChangeRequest = new ServiceChangeRequest();
        serviceChangeRequest.setChangeId(request.getId());
        if (request.getServiceOrderEntity().getTaskType() == ServiceOrderType.CONFIG_CHANGE) {
            Optional<ServiceChangeScript> configManageScriptOptional =
                    this.serviceConfigurationManager.getConfigManageScript(request);
            configManageScriptOptional.ifPresent(
                    serviceChangeScript ->
                            serviceChangeRequest.setAnsibleScriptConfig(
                                    serviceChangeScript.getAnsibleScriptConfig()));
        } else if (request.getServiceOrderEntity().getTaskType()
                == ServiceOrderType.SERVICE_ACTION) {
            Optional<ServiceChangeScript> changeManageScriptOptional =
                    this.serviceActionManager.getServiceActionManageScript(request);
            changeManageScriptOptional.ifPresent(
                    serviceChangeScript ->
                            serviceChangeRequest.setAnsibleScriptConfig(
                                    serviceChangeScript.getAnsibleScriptConfig()));
        }

        serviceChangeRequest.setServiceChangeParameters(request.getProperties());
        serviceChangeRequest.setAnsibleInventory(getAnsibleInventory(deployResources));
        return serviceChangeRequest;
    }

    /**
     * Method to update service change result.
     *
     * @param changeId id of the update request.
     * @param result result of the service change request.
     */
    public void updateServiceChangeResult(UUID changeId, ServiceChangeResult result) {
        ServiceChangeRequestEntity request = serviceChangeRequestStorage.findById(changeId);
        if (Objects.isNull(request)
                || !ServiceChangeStatus.PROCESSING.equals(request.getStatus())) {
            String errorMsg =
                    String.format(
                            "Service change details with id %s , status %s not found",
                            changeId, ServiceChangeStatus.PROCESSING);
            log.error(errorMsg);
            throw new ServiceChangeRequestEntityNotFoundException(errorMsg);
        }
        if (result.getIsSuccessful()) {
            request.setStatus(ServiceChangeStatus.SUCCESSFUL);
        } else {
            request.setStatus(ServiceChangeStatus.ERROR);
            request.setResultMessage(result.getError());
        }
        request.setTasks(result.getTasks());
        serviceChangeRequestStorage.storeAndFlush(request);
        updateServiceChangeResult(request);
    }

    private void updateServiceChangeResult(ServiceChangeRequestEntity request) {
        ServiceChangeRequestQueryModel model =
                new ServiceChangeRequestQueryModel(
                        request.getServiceOrderEntity().getOrderId(),
                        request.getServiceDeploymentEntity().getId(),
                        null,
                        null,
                        null);
        List<ServiceChangeRequestEntity> requests =
                serviceChangeRequestStorage.getServiceChangeRequestEntities(model);

        if (CollectionUtils.isEmpty(requests)) {
            String errorMsg =
                    String.format(
                            "Service configuration change details with service "
                                    + "id %s not found, ",
                            request.getServiceDeploymentEntity().getId());
            log.error(errorMsg);
            throw new ServiceChangeRequestEntityNotFoundException(errorMsg);
        }
        boolean isNeedUpdateServiceConfigurationChange =
                requests.stream()
                        .allMatch(
                                changeDetails ->
                                        changeDetails.getStatus() == ServiceChangeStatus.SUCCESSFUL
                                                || changeDetails.getStatus()
                                                        == ServiceChangeStatus.ERROR);
        if (isNeedUpdateServiceConfigurationChange) {
            ServiceOrderEntity entity = request.getServiceOrderEntity();
            if (Objects.nonNull(entity)) {
                boolean isAllSuccessful =
                        requests.stream()
                                .allMatch(
                                        changeDetails ->
                                                request.getStatus()
                                                        == ServiceChangeStatus.SUCCESSFUL);
                if (isAllSuccessful) {
                    updateServiceOrderByResult(entity, TaskStatus.SUCCESSFUL, null);
                    // update configuration only if the job all requests are successful.
                    if (request.getServiceOrderEntity().getTaskType()
                            == ServiceOrderType.CONFIG_CHANGE) {
                        serviceConfigurationManager.updateServiceConfigurationInDatabase(
                                request, request.getOriginalRequestProperties());
                    }
                } else {
                    updateServiceOrderByResult(
                            entity, TaskStatus.FAILED, request.getResultMessage());
                }
            }
        }
    }

    private void updateServiceOrderByResult(
            ServiceOrderEntity entity, TaskStatus status, String resultMessage) {
        log.debug(
                "updating service order result for the service change order {} with status {}",
                entity.getOrderId(),
                status.toValue());
        serviceOrderManager.completeOrderProgress(
                entity.getOrderId(),
                status,
                Objects.nonNull(resultMessage)
                        ? ErrorResponse.errorResponse(
                                ErrorType.SERVICE_CHANGE_FAILED, List.of(resultMessage))
                        : null);
    }

    private void validateChangeHandler(
            UUID serviceId,
            String configManager,
            String resourceName,
            List<DeployResource> deployResources) {
        Map<String, List<DeployResource>> resourceNameMap =
                deployResources.stream()
                        .collect(Collectors.groupingBy(DeployResource::getResourceName));
        if (!resourceNameMap.containsKey(resourceName)) {
            String errorMsg =
                    String.format(
                            "The service with serviceId %s does not contain "
                                    + "a resource with resource_name %s",
                            serviceId, resourceName);
            log.error(errorMsg);
            throw new ServiceConfigurationInvalidException(List.of(errorMsg));
        }
        deployResources.forEach(
                deployResource -> {
                    if (resourceName.equals(deployResource.getResourceName())) {
                        if (!configManager.equals(deployResource.getGroupName())) {
                            String errorMsg =
                                    String.format(
                                            "The service with serviceId %s does"
                                                    + " not contain a group with resource_name %s "
                                                    + "and  group_name %s",
                                            serviceId, resourceName, configManager);
                            log.error(errorMsg);
                            throw new ServiceConfigurationInvalidException(List.of(errorMsg));
                        }
                    }
                });
    }

    private Map<String, Object> getAnsibleInventory(List<DeployResource> deployResources) {
        Map<String, List<DeployResource>> deployResourceMap =
                deployResources.stream()
                        .collect(Collectors.groupingBy(DeployResource::getGroupName));
        Map<String, Object> ansibleInventory = new HashMap<>();
        deployResourceMap.forEach(
                (groupName, deployResourceList) -> {
                    Map<String, AnsibleHostInfo> hosts = new HashMap<>();
                    deployResourceList.forEach(
                            deployResource -> {
                                AnsibleHostInfo ansibleHostInfo = new AnsibleHostInfo();
                                ansibleHostInfo.setAnsibleHost(
                                        deployResource.getProperties().get(IP));
                                hosts.put(deployResource.getResourceName(), ansibleHostInfo);
                            });
                    Map<String, Map<String, AnsibleHostInfo>> resourceMap = new HashMap<>();
                    resourceMap.put(HOSTS, hosts);
                    ansibleInventory.put(groupName, resourceMap);
                });
        return ansibleInventory;
    }

    private List<DeployResource> getDeployResources(
            UUID serviceId, DeployResourceKind resourceKind) {
        ServiceDeploymentEntity deployedService =
                serviceDeploymentEntityHandler.getServiceDeploymentEntity(serviceId);
        Stream<ServiceResourceEntity> resourceEntities =
                deployedService.getDeployResources().stream();
        if (Objects.nonNull(resourceKind)) {
            resourceEntities =
                    resourceEntities.filter(
                            resourceEntity ->
                                    resourceEntity.getResourceKind().equals(resourceKind));
        }
        return EntityTranslationUtils.transToDeployResources(resourceEntities.toList());
    }
}
