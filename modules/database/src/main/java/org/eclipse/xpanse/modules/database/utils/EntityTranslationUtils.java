/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.resource.ServiceResourceEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeRequestEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.servicetemplaterequest.ServiceTemplateRequestHistoryEntity;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderDetails;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.service.view.VendorHostedDeployedServiceDetails;
import org.eclipse.xpanse.modules.models.servicechange.ServiceChangeOrderDetails;
import org.eclipse.xpanse.modules.models.servicechange.ServiceChangeRequestDetails;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestHistory;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestToReview;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

/** Transform DB entity object and model object. */
@Slf4j
public class EntityTranslationUtils {

    private EntityTranslationUtils() {
        // block constructor.
    }

    /**
     * Transform list of deployResourceEntity to list of DeployResource.
     *
     * @param entities list of deployResourceEntity
     * @return list of DeployResource
     */
    public static List<DeployResource> transToDeployResources(
            List<ServiceResourceEntity> entities) {
        List<DeployResource> resources = new ArrayList<>();
        if (!CollectionUtils.isEmpty(entities)) {
            for (ServiceResourceEntity entity : entities) {
                DeployResource deployResource = new DeployResource();
                BeanUtils.copyProperties(entity, deployResource);
                resources.add(deployResource);
            }
        }
        return resources;
    }

    /**
     * DeployServiceEntity converted to DeployedService.
     *
     * @param serviceEntity DeployServiceEntity
     * @return result
     */
    public static DeployedService convertToDeployedService(ServiceDeploymentEntity serviceEntity) {
        if (Objects.nonNull(serviceEntity)) {
            DeployedService deployedService = new DeployedService();
            BeanUtils.copyProperties(serviceEntity, deployedService);
            deployedService.setServiceId(serviceEntity.getId());
            return deployedService;
        }
        return null;
    }

    /**
     * DeployServiceEntity converted to DeployedServiceDetails.
     *
     * @param entity DeployServiceEntity.
     * @return DeployedServiceDetails
     */
    public static DeployedServiceDetails transToDeployedServiceDetails(
            ServiceDeploymentEntity entity) {
        DeployedServiceDetails details = new DeployedServiceDetails();
        BeanUtils.copyProperties(entity, details);
        details.setServiceId(entity.getId());
        if (!CollectionUtils.isEmpty(entity.getDeployResources())) {
            details.setDeployResources(transToDeployResources(entity.getDeployResources()));
        }
        if (!CollectionUtils.isEmpty(entity.getOutputProperties())) {
            details.setDeployedServiceProperties(entity.getOutputProperties());
        }
        return details;
    }

    /**
     * DeployServiceEntity converted to VendorHostedDeployedServiceDetails.
     *
     * @param entity DeployServiceEntity.
     * @return serviceDetailVo
     */
    public static VendorHostedDeployedServiceDetails transToVendorHostedServiceDetails(
            ServiceDeploymentEntity entity) {
        VendorHostedDeployedServiceDetails details = new VendorHostedDeployedServiceDetails();
        BeanUtils.copyProperties(entity, details);
        details.setServiceId(entity.getId());
        if (!CollectionUtils.isEmpty(entity.getOutputProperties())) {
            details.setDeployedServiceProperties(entity.getOutputProperties());
        }
        return details;
    }

    /**
     * ServiceOrderEntity converted to ServiceOrderDetails.
     *
     * @param entity ServiceOrderEntity
     * @return ServiceOrderDetails
     */
    public static ServiceOrderDetails transToServiceOrderDetails(ServiceOrderEntity entity) {
        ServiceOrderDetails details = new ServiceOrderDetails();
        BeanUtils.copyProperties(entity, details);
        details.setServiceId(entity.getServiceDeploymentEntity().getId());
        return details;
    }

    /**
     * ServiceConfigurationEntity converted to ServiceConfigurationDetails.
     *
     * @param entity ServiceConfigurationEntity
     * @return ServiceConfigurationDetails
     */
    public static ServiceConfigurationDetails transToServiceConfigurationDetails(
            ServiceConfigurationEntity entity) {
        ServiceConfigurationDetails details = new ServiceConfigurationDetails();
        BeanUtils.copyProperties(entity, details);
        return details;
    }

    /**
     * Collection of ServiceChangeDetailsEntity converted to Collection of
     * ServiceChangeOrderDetails.
     *
     * @param requests Collection of ServiceChangeDetailsEntity.
     * @return Collection of ServiceChangeOrderDetails.
     */
    public static List<ServiceChangeOrderDetails> transToServiceChangeOrderDetails(
            List<ServiceChangeRequestEntity> requests) {

        Map<UUID, List<ServiceChangeRequestEntity>> orderDetailsMap =
                requests.stream()
                        .collect(
                                Collectors.groupingBy(
                                        request -> request.getServiceOrderEntity().getOrderId()));
        List<ServiceChangeOrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsMap.forEach(
                (orderId, requestList) -> {
                    ServiceChangeOrderDetails orderDetails = new ServiceChangeOrderDetails();
                    orderDetails.setOrderId(orderId);
                    ServiceOrderEntity orderEntity = requestList.getFirst().getServiceOrderEntity();
                    orderDetails.setServiceChangeRequestProperties(orderEntity.getRequestBody());
                    orderDetails.setOrderStatus(orderEntity.getTaskStatus());
                    List<ServiceChangeRequestDetails> detailsList = new ArrayList<>();
                    requestList.forEach(
                            request -> {
                                ServiceChangeRequestDetails details =
                                        new ServiceChangeRequestDetails();
                                details.setChangeId(request.getId());
                                details.setResourceName(request.getResourceName());
                                details.setChangeHandler(request.getChangeHandler());
                                details.setResultMessage(request.getResultMessage());
                                details.setProperties(request.getProperties());
                                details.setStatus(request.getStatus());
                                detailsList.add(details);
                            });
                    orderDetails.setServiceChangeRequests(detailsList);
                    orderDetailsList.add(orderDetails);
                });
        return orderDetailsList;
    }

    /**
     * ServiceTemplateRequestHistoryEntity converted to ServiceTemplateRequestHistory.
     *
     * @param serviceTemplateRequestHistoryEntity ServiceTemplateRequestHistoryEntity.
     * @return serviceTemplateHistoryVo
     */
    public static ServiceTemplateRequestHistory convertToServiceTemplateHistoryVo(
            ServiceTemplateRequestHistoryEntity serviceTemplateRequestHistoryEntity) {
        ServiceTemplateRequestHistory serviceTemplateRequestHistory =
                new ServiceTemplateRequestHistory();
        serviceTemplateRequestHistory.setServiceTemplateId(
                serviceTemplateRequestHistoryEntity.getServiceTemplate().getId());
        BeanUtils.copyProperties(
                serviceTemplateRequestHistoryEntity, serviceTemplateRequestHistory);
        serviceTemplateRequestHistory.setRequestSubmittedForReview(
                serviceTemplateRequestHistoryEntity.getRequestStatus()
                        == ServiceTemplateRequestStatus.IN_REVIEW);
        return serviceTemplateRequestHistory;
    }

    /**
     * ServiceTemplateRequestHistoryEntity converted to ServiceTemplateRequestToReview.
     *
     * @param requestEntity ServiceTemplateRequestHistoryEntity.
     * @return serviceTemplateRequestVo
     */
    public static ServiceTemplateRequestToReview convertToServiceTemplateRequestVo(
            ServiceTemplateRequestHistoryEntity requestEntity) {
        ServiceTemplateRequestToReview requestVo = new ServiceTemplateRequestToReview();
        requestVo.setServiceTemplateId(requestEntity.getServiceTemplate().getId());
        BeanUtils.copyProperties(requestEntity, requestVo);
        requestVo.setRequestSubmittedForReview(
                requestEntity.getRequestStatus() == ServiceTemplateRequestStatus.IN_REVIEW);
        return requestVo;
    }
}
