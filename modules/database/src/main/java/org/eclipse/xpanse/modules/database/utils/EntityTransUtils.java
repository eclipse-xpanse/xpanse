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
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceConfigurationChangeDetailsEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.servicetemplatehistory.ServiceTemplateHistoryEntity;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderDetails;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.service.view.VendorHostedDeployedServiceDetails;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationChangeDetails;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationChangeOrderDetails;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.change.ServiceTemplateHistoryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

/**
 * Transform DB entity object and model object.
 */
@Slf4j
public class EntityTransUtils {

    private EntityTransUtils() {
        // block constructor.
    }


    /**
     * Transform list of deployResourceEntity to list of DeployResource.
     *
     * @param entities list of deployResourceEntity
     * @return list of DeployResource
     */
    public static List<DeployResource> transToDeployResourceList(
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
            deployedService.setServiceHostingType(
                    serviceEntity.getDeployRequest().getServiceHostingType());
            deployedService.setRegion(serviceEntity.getDeployRequest().getRegion());
            deployedService.setBillingMode(serviceEntity.getDeployRequest().getBillingMode());
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
        details.setServiceHostingType(entity.getDeployRequest().getServiceHostingType());
        details.setBillingMode(entity.getDeployRequest().getBillingMode());
        details.setRegion(entity.getDeployRequest().getRegion());
        BeanUtils.copyProperties(entity, details);
        details.setServiceId(entity.getId());
        if (!CollectionUtils.isEmpty(entity.getDeployResourceList())) {
            details.setDeployResources(transToDeployResourceList(entity.getDeployResourceList()));
        }
        if (!CollectionUtils.isEmpty(entity.getOutputProperties())) {
            details.setDeployedServiceProperties(entity.getOutputProperties());
        }
        if (Objects.nonNull(entity.getServiceTemplateId())) {
            details.setServiceTemplateId(entity.getServiceTemplateId());
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
        details.setServiceHostingType(entity.getDeployRequest().getServiceHostingType());
        BeanUtils.copyProperties(entity, details);
        details.setServiceId(entity.getId());
        details.setBillingMode(entity.getDeployRequest().getBillingMode());
        details.setRegion(entity.getDeployRequest().getRegion());
        if (!CollectionUtils.isEmpty(entity.getOutputProperties())) {
            details.setDeployedServiceProperties(entity.getOutputProperties());
        }
        if (Objects.nonNull(entity.getServiceTemplateId())) {
            details.setServiceTemplateId(entity.getServiceTemplateId());
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
     * Collection of ServiceConfigurationChangeDetailsEntity converted to
     * Collection of ServiceConfigurationUpdateRequestOrderDetails.
     *
     * @param requests Collection of ServiceConfigurationChangeDetailsEntity.
     * @return Collection of ServiceConfigurationUpdateRequestOrderDetails.
     */
    public static List<ServiceConfigurationChangeOrderDetails>
            transToServiceConfigurationChangeOrderDetails(
                List<ServiceConfigurationChangeDetailsEntity> requests) {

        Map<UUID, List<ServiceConfigurationChangeDetailsEntity>> orderDetailsMap = requests.stream()
                .collect(Collectors.groupingBy(
                        request -> request.getServiceOrderEntity().getOrderId()));
        List<ServiceConfigurationChangeOrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsMap.forEach((orderId, requestList) -> {
            ServiceConfigurationChangeOrderDetails orderDetails =
                    new ServiceConfigurationChangeOrderDetails();
            orderDetails.setOrderId(orderId);
            ServiceOrderEntity orderEntity = requestList.getFirst().getServiceOrderEntity();
            orderDetails.setConfigRequest(orderEntity.getNewConfigRequest());
            orderDetails.setOrderStatus(orderEntity.getTaskStatus());
            List<ServiceConfigurationChangeDetails> detailsList = new ArrayList<>();
            requestList.forEach(request -> {
                ServiceConfigurationChangeDetails details =
                        new ServiceConfigurationChangeDetails();
                details.setChangeId(request.getId());
                details.setResourceName(request.getResourceName());
                details.setConfigManager(request.getConfigManager());
                details.setResultMessage(request.getResultMessage());
                details.setProperties(request.getProperties());
                details.setStatus(request.getStatus());
                detailsList.add(details);
            });
            orderDetails.setChangeRequests(detailsList);
            orderDetailsList.add(orderDetails);
        });
        return orderDetailsList;
    }


    /**
     * ServiceTemplateHistoryEntity converted to ServiceTemplateHistoryVo.
     *
     * @param serviceTemplateHistoryEntity ServiceTemplateHistoryEntity.
     * @return serviceTemplateHistoryVo
     */
    public static ServiceTemplateHistoryVo convertToServiceTemplateHistoryVo(
            ServiceTemplateHistoryEntity serviceTemplateHistoryEntity) {
        ServiceTemplateHistoryVo serviceTemplateHistoryVo = new ServiceTemplateHistoryVo();
        serviceTemplateHistoryVo.setServiceTemplateId(
                serviceTemplateHistoryEntity.getServiceTemplate().getId());
        BeanUtils.copyProperties(serviceTemplateHistoryEntity, serviceTemplateHistoryVo);
        return serviceTemplateHistoryVo;
    }
}
