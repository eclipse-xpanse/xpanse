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
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeRequestEntity;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeRequestQueryModel;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeRequestStorage;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.utils.EntityTranslationUtils;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.Handler;
import org.eclipse.xpanse.modules.models.service.enums.OrderStatus;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.servicechange.ServiceChangeOrderDetails;
import org.eclipse.xpanse.modules.models.servicechange.enums.ServiceChangeStatus;
import org.eclipse.xpanse.modules.models.servicechange.exceptions.ServiceChangeRequestEntityNotFoundException;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeScript;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Bean to manage service change requests. */
@Slf4j
@Component
public class ServiceChangeRequestsManager {

    @Resource private ServiceChangeRequestStorage serviceChangeRequestStorage;

    @Resource private ServiceOrderManager serviceOrderManager;

    /**
     * creates one service order and then one or more service change requests in database depending
     * on the service template configuration and the changes requested by the user. The method also
     * creates a corresponding service order for the same.
     *
     * @param serviceDeploymentEntity serviceDeploymentEntity for which the change is requested.
     * @param originalRequestObject full request object received from the user.
     * @param originalRequestProperties request properties received from the user. This is part of
     *     the original request object.
     * @param finalPropertiesToBeUsed Actual properties to be used in the change request.
     * @param deployResourceMap Map of resources to change handlers.
     * @param serviceChangeScripts service change scripts available depending on the request.
     *     Example, in case of actions, this contains the change scripts specific for that action.
     *     In case of config change, this contains the scripts only for the specific config
     *     parameters that are requested to be changed.
     * @param serviceOrderType type of the service order.
     */
    public UUID createServiceOrderAndQueueServiceChangeRequests(
            ServiceDeploymentEntity serviceDeploymentEntity,
            Object originalRequestObject,
            Map<String, Object> originalRequestProperties,
            Map<String, Object> finalPropertiesToBeUsed,
            Map<String, List<DeployResource>> deployResourceMap,
            List<ServiceChangeScript> serviceChangeScripts,
            ServiceOrderType serviceOrderType,
            Handler handler) {
        ServiceOrderEntity serviceOrderEntity =
                serviceOrderManager.createAndStoreGenericServiceOrderEntity(
                        serviceDeploymentEntity, serviceOrderType, originalRequestObject, handler);
        List<ServiceChangeRequestEntity> requests = new ArrayList<>();
        deployResourceMap.forEach(
                (groupName, deployResourceList) ->
                        serviceChangeScripts.forEach(
                                serviceChangeScript -> {
                                    if (serviceChangeScript.getChangeHandler().equals(groupName)) {
                                        if (!CollectionUtils.isEmpty(deployResourceList)) {
                                            if (serviceChangeScript.getRunOnlyOnce()) {
                                                ServiceChangeRequestEntity request =
                                                        createServiceChangeRequestEntity(
                                                                groupName,
                                                                serviceDeploymentEntity,
                                                                finalPropertiesToBeUsed,
                                                                originalRequestProperties,
                                                                serviceOrderEntity);
                                                request.setResourceName(
                                                        deployResourceList
                                                                .getFirst()
                                                                .getResourceName());
                                                requests.add(request);
                                            } else {
                                                deployResourceList.forEach(
                                                        deployResource -> {
                                                            ServiceChangeRequestEntity request =
                                                                    createServiceChangeRequestEntity(
                                                                            groupName,
                                                                            serviceDeploymentEntity,
                                                                            finalPropertiesToBeUsed,
                                                                            originalRequestProperties,
                                                                            serviceOrderEntity);
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
            serviceChangeRequestStorage.saveAll(requests);
        } else {
            // if no requests were created, then the order is completed as failed.
            serviceOrderManager.completeOrderProgress(
                    serviceOrderEntity.getOrderId(),
                    OrderStatus.FAILED,
                    ErrorResponse.errorResponse(
                            ErrorType.SERVICE_CHANGE_FAILED,
                            List.of(
                                    "No change requests created for the requested change"
                                            + " properties.")));
        }
        return serviceOrderEntity.getOrderId();
    }

    /** Query service change details update request by queryModel. */
    public List<ServiceChangeOrderDetails> getAllChangeRequests(
            String orderId,
            String serviceId,
            String resourceName,
            String changeHandler,
            ServiceChangeStatus status) {
        UUID uuidOrderId = StringUtils.isEmpty(orderId) ? null : UUID.fromString(orderId);
        ServiceChangeRequestQueryModel queryModel =
                ServiceChangeRequestQueryModel.builder()
                        .orderId(uuidOrderId)
                        .serviceId(UUID.fromString(serviceId))
                        .resourceName(resourceName)
                        .changeHandler(changeHandler)
                        .status(status)
                        .build();
        List<ServiceChangeRequestEntity> requests =
                serviceChangeRequestStorage.getServiceChangeRequestEntities(queryModel);

        if (CollectionUtils.isEmpty(requests)) {
            String errorMsg =
                    String.format(
                            "Service change details request with service id %s not found, ",
                            serviceId);
            log.error(errorMsg);
            throw new ServiceChangeRequestEntityNotFoundException(errorMsg);
        }
        return EntityTranslationUtils.transToServiceChangeOrderDetails(requests);
    }

    private ServiceChangeRequestEntity createServiceChangeRequestEntity(
            String groupName,
            ServiceDeploymentEntity entity,
            Map<String, Object> properties,
            Map<String, Object> originalPropertiesReceived,
            ServiceOrderEntity serviceOrderEntity) {
        ServiceChangeRequestEntity request = new ServiceChangeRequestEntity();
        request.setServiceOrderEntity(serviceOrderEntity);
        request.setServiceDeploymentEntity(entity);
        request.setChangeHandler(groupName);
        request.setProperties(properties);
        request.setOriginalRequestProperties(originalPropertiesReceived);
        request.setStatus(ServiceChangeStatus.PENDING);
        request.setCreatedTime(OffsetDateTime.now());
        return request;
    }
}
