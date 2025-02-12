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
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeDetailsEntity;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeDetailsQueryModel;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeDetailsStorage;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.servicechange.ServiceChangeOrderDetails;
import org.eclipse.xpanse.modules.models.servicechange.enums.ServiceChangeStatus;
import org.eclipse.xpanse.modules.models.servicechange.exceptions.ServiceChangeDetailsEntityNotFoundException;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeParameter;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeScript;
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Bean to manage service change details. */
@Slf4j
@Component
public class ServiceChangeDetailsManager {

    @Resource private ServiceChangeDetailsStorage serviceChangeDetailsStorage;

    @Resource private ServiceOrderStorage serviceOrderStorage;

    @Resource private UserServiceHelper userServiceHelper;

    /**
     * Creates one or more service change requests in database depending on the service template
     * configuration.
     */
    public void createAndQueueAllServiceChangeRequests(
            UUID orderId,
            ServiceDeploymentEntity serviceDeployment,
            Map<String, Object> userRequestedProperties,
            Map<String, List<DeployResource>> deployResourceMap,
            List<ServiceChangeScript> serviceChangeScripts,
            List<ServiceChangeParameter> serviceChangeParameters,
            ServiceOrderType type) {

        List<ServiceChangeDetailsEntity> requests = new ArrayList<>();
        deployResourceMap.forEach(
                (groupName, deployResourceList) ->
                        serviceChangeScripts.forEach(
                                serviceChangeScript -> {
                                    if (serviceChangeScript.getChangeHandler().equals(groupName)) {
                                        if (!CollectionUtils.isEmpty(deployResourceList)) {
                                            Map<String, Object> properties =
                                                    buildFullRequestPropertiesFromServiceTemplateData(
                                                            groupName,
                                                            serviceChangeParameters,
                                                            userRequestedProperties);
                                            if (serviceChangeScript.getRunOnlyOnce()) {
                                                ServiceChangeDetailsEntity request =
                                                        createServiceChangeDetailEntity(
                                                                orderId,
                                                                groupName,
                                                                serviceDeployment,
                                                                properties,
                                                                userRequestedProperties,
                                                                type);
                                                requests.add(request);
                                            } else {
                                                deployResourceList.forEach(
                                                        deployResource -> {
                                                            ServiceChangeDetailsEntity request =
                                                                    createServiceChangeDetailEntity(
                                                                            orderId,
                                                                            groupName,
                                                                            serviceDeployment,
                                                                            properties,
                                                                            userRequestedProperties,
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
        if (!requests.isEmpty()) {
            serviceChangeDetailsStorage.saveAll(requests);
        }
    }

    /** Query service change details update request by queryModel. */
    public List<ServiceChangeOrderDetails> getAllChangeRequests(
            String orderId,
            String serviceId,
            String resourceName,
            String changeHandler,
            ServiceChangeStatus status) {
        UUID uuidOrderId = StringUtils.isEmpty(orderId) ? null : UUID.fromString(orderId);
        ServiceChangeDetailsQueryModel queryModel =
                new ServiceChangeDetailsQueryModel(
                        uuidOrderId,
                        UUID.fromString(serviceId),
                        resourceName,
                        changeHandler,
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

    private Map<String, Object> buildFullRequestPropertiesFromServiceTemplateData(
            String groupName,
            List<ServiceChangeParameter> params,
            Map<String, Object> updateRequestMap) {

        Map<String, Object> fullServiceConfig = new HashMap<>();
        // build map with initial values defined in service template.
        params.forEach(
                serviceChangeParameter -> {
                    if (groupName.equals(serviceChangeParameter.getManagedBy())) {
                        fullServiceConfig.put(
                                serviceChangeParameter.getName(),
                                serviceChangeParameter.getInitialValue());
                    }
                });
        // override initial values with values requested by the user.
        updateRequestMap.forEach(
                (k, v) -> {
                    if (fullServiceConfig.containsKey(k)) {
                        fullServiceConfig.put(k, v);
                    }
                });
        return fullServiceConfig;
    }

    private ServiceChangeDetailsEntity createServiceChangeDetailEntity(
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
        if (Objects.nonNull(entity.getServiceOrders())) {
            entity.getServiceOrders().add(serviceOrderEntity);
        } else {
            entity.setServiceOrders(List.of(serviceOrderEntity));
        }
        serviceOrderEntity.setServiceDeploymentEntity(entity);
        serviceOrderEntity.setTaskType(type);
        serviceOrderEntity.setUserId(userServiceHelper.getCurrentUserId());
        serviceOrderEntity.setTaskStatus(TaskStatus.CREATED);
        serviceOrderEntity.setStartedTime(OffsetDateTime.now());
        serviceOrderEntity.setRequestBody(updateRequestMap);
        return serviceOrderStorage.storeAndFlush(serviceOrderEntity);
    }
}
