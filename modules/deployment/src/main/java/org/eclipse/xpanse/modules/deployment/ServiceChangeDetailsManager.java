/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceChangeDetailsEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceChangeDetailsQueryModel;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceChangeDetailsStorage;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.Handler;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceChangeOrderDetails;
import org.eclipse.xpanse.modules.models.serviceconfiguration.enums.ServiceChangeStatus;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceChangeDetailsEntityNotFoundException;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeParameter;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeScript;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Bean to manage service change details. */
@Slf4j
@Component
public class ServiceChangeDetailsManager {

    @Resource private ServiceChangeDetailsStorage serviceChangeDetailsStorage;

    @Resource private ServiceOrderStorage serviceOrderStorage;

    @Resource private UserServiceHelper userServiceHelper;

    /** get service change details. */
    public List<ServiceChangeDetailsEntity> getAllServiceChangeDetails(
            UUID orderId,
            ServiceDeploymentEntity serviceDeployment,
            Map<String, Object> updateRequestMap,
            Map<String, List<DeployResource>> deployResourceMap,
            List<ServiceChangeScript> configManageScripts,
            List<ServiceChangeParameter> configurationParameters,
            ServiceOrderType type) {

        List<ServiceChangeDetailsEntity> requests = new ArrayList<>();
        deployResourceMap.forEach(
                (groupName, deployResourceList) ->
                        configManageScripts.forEach(
                                serviceChangeScript -> {
                                    if (serviceChangeScript.getChangeHandler().equals(groupName)) {
                                        if (!CollectionUtils.isEmpty(deployResourceList)) {
                                            Map<String, Object> properties =
                                                    getServiceChangeDetailsProperties(
                                                            groupName,
                                                            configurationParameters,
                                                            updateRequestMap);
                                            if (serviceChangeScript.getRunOnlyOnce()) {
                                                ServiceChangeDetailsEntity request =
                                                        getServiceChangeDetails(
                                                                orderId,
                                                                groupName,
                                                                serviceDeployment,
                                                                properties,
                                                                updateRequestMap,
                                                                type);
                                                requests.add(request);
                                            } else {
                                                deployResourceList.forEach(
                                                        deployResource -> {
                                                            ServiceChangeDetailsEntity request =
                                                                    getServiceChangeDetails(
                                                                            orderId,
                                                                            groupName,
                                                                            serviceDeployment,
                                                                            properties,
                                                                            updateRequestMap,
                                                                            type);
                                                            request.setResourceName(
                                                                    deployResource
                                                                            .getResourceName());
                                                            requests.add(request);
                                                        });
                                            }
                                        }
                                    }
                                }));
        return requests;
    }

    /** Query service change details update request by queryModel. */
    public List<ServiceChangeOrderDetails> getServiceChangeRequestDetails(
            String orderId,
            String serviceId,
            String resourceName,
            String configManager,
            ServiceChangeStatus status) {
        UUID uuidOrderId = StringUtils.isEmpty(orderId) ? null : UUID.fromString(orderId);
        ServiceChangeDetailsQueryModel queryModel =
                new ServiceChangeDetailsQueryModel(
                        uuidOrderId,
                        UUID.fromString(serviceId),
                        resourceName,
                        configManager,
                        status);
        List<ServiceChangeDetailsEntity> requests =
                serviceChangeDetailsStorage.listServiceChangeDetails(queryModel);

        if (CollectionUtils.isEmpty(requests)) {
            String errorMsg =
                    String.format(
                            "Service change details request with service id %s not found, ",
                            serviceId);
            log.error(errorMsg);
            throw new ServiceChangeDetailsEntityNotFoundException(errorMsg);
        }
        return EntityTransUtils.transToServiceChangeOrderDetails(requests);
    }

    private Map<String, Object> getServiceChangeDetailsProperties(
            String groupName,
            List<ServiceChangeParameter> params,
            Map<String, Object> updateRequestMap) {

        Map<String, Object> existsServiceConfig = new HashMap<>();
        params.forEach(
                serviceConfigurationParameter -> {
                    if (groupName.equals(serviceConfigurationParameter.getManagedBy())) {
                        existsServiceConfig.put(
                                serviceConfigurationParameter.getName(),
                                serviceConfigurationParameter.getInitialValue());
                    }
                });
        updateRequestMap.forEach(
                (k, v) -> {
                    if (existsServiceConfig.containsKey(k)) {
                        existsServiceConfig.put(k, v);
                    }
                });
        return existsServiceConfig;
    }

    private ServiceChangeDetailsEntity getServiceChangeDetails(
            UUID orderId,
            String groupName,
            ServiceDeploymentEntity entity,
            Map<String, Object> properties,
            Map<String, Object> updateRequestMap,
            ServiceOrderType type) {

        ServiceChangeDetailsEntity request = new ServiceChangeDetailsEntity();
        ServiceOrderEntity serviceOrderEntity =
                saveServiceOrder(orderId, entity, type, updateRequestMap);
        request.setServiceOrderEntity(serviceOrderEntity);
        request.setServiceDeploymentEntity(entity);
        request.setChangeHandler(groupName);
        request.setProperties(properties);
        request.setStatus(ServiceChangeStatus.PENDING);
        return request;
    }

    private ServiceOrderEntity saveServiceOrder(
            UUID orderId,
            ServiceDeploymentEntity entity,
            ServiceOrderType type,
            Map<String, Object> updateRequestMap) {
        ServiceOrderEntity serviceOrderEntity = new ServiceOrderEntity();
        serviceOrderEntity.setOrderId(orderId);
        if (Objects.nonNull(entity.getServiceOrderList())) {
            entity.getServiceOrderList().add(serviceOrderEntity);
        } else {
            entity.setServiceOrderList(List.of(serviceOrderEntity));
        }
        serviceOrderEntity.setServiceDeploymentEntity(entity);
        serviceOrderEntity.setTaskType(type);
        serviceOrderEntity.setUserId(userServiceHelper.getCurrentUserId());
        serviceOrderEntity.setTaskStatus(TaskStatus.CREATED);
        serviceOrderEntity.setStartedTime(OffsetDateTime.now());
        serviceOrderEntity.setRequestBody(updateRequestMap);
        serviceOrderEntity.setHandler(Handler.AGENT);
        return serviceOrderStorage.storeAndFlush(serviceOrderEntity);
    }
}
