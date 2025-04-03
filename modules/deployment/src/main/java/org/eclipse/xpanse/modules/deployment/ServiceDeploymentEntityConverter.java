/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeManage;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeParameter;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavorWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateUnavailableException;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Bean to create DeployTask object from DeployServiceEntity. This is needed for everything other
 * than deploy tasks.
 */
@Slf4j
@Component
public class ServiceDeploymentEntityConverter {

    /**
     * Method to create a DeployTask from DeployServiceEntity.
     *
     * @param orderType ServiceOrderType.
     * @param serviceDeploymentEntity DeployServiceEntity object.
     * @return DeployTask object.
     */
    public DeployTask getDeployTaskByStoredService(
            ServiceOrderType orderType, ServiceDeploymentEntity serviceDeploymentEntity) {
        // Set Ocl and CreateRequest
        DeployTask deployTask = new DeployTask();
        deployTask.setTaskType(orderType);
        deployTask.setServiceId(serviceDeploymentEntity.getId());
        deployTask.setUserId(serviceDeploymentEntity.getUserId());
        deployTask.setDeployRequest(getDeployRequestByStoredService(serviceDeploymentEntity));
        deployTask.setServiceVendor(serviceDeploymentEntity.getServiceVendor());
        ServiceTemplateEntity serviceTemplateEntity =
                serviceDeploymentEntity.getServiceTemplateEntity();
        if (!serviceTemplateEntity.getIsAvailableInCatalog()) {
            String errorMsg =
                    String.format(
                            "Used service template %s is unavailable to be used to %s service.",
                            serviceTemplateEntity.getId(), orderType.toValue());
            log.error(errorMsg);
            throw new ServiceTemplateUnavailableException(errorMsg);
        }
        deployTask.setOcl(serviceTemplateEntity.getOcl());
        deployTask.setServiceTemplateId(serviceTemplateEntity.getId());
        return deployTask;
    }

    /**
     * Method to get deployRequest from service deployment.
     *
     * @param serviceDeploymentEntity serviceDeploymentEntity
     * @return DeployRequest
     */
    public DeployRequest getDeployRequestByStoredService(
            ServiceDeploymentEntity serviceDeploymentEntity) {
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setCustomerServiceName(serviceDeploymentEntity.getCustomerServiceName());
        deployRequest.setCategory(serviceDeploymentEntity.getCategory());
        deployRequest.setServiceName(serviceDeploymentEntity.getName());
        deployRequest.setVersion(serviceDeploymentEntity.getVersion());
        deployRequest.setCsp(serviceDeploymentEntity.getCsp());
        deployRequest.setServiceHostingType(serviceDeploymentEntity.getServiceHostingType());
        deployRequest.setRegion(serviceDeploymentEntity.getRegion());
        deployRequest.setFlavor(serviceDeploymentEntity.getFlavor());
        deployRequest.setAvailabilityZones(serviceDeploymentEntity.getAvailabilityZones());
        deployRequest.setBillingMode(serviceDeploymentEntity.getBillingMode());
        deployRequest.setEulaAccepted(serviceDeploymentEntity.getIsEulaAccepted());
        if (!CollectionUtils.isEmpty(serviceDeploymentEntity.getInputProperties())) {
            Map<String, Object> serviceRequestProperties =
                    new HashMap<>(serviceDeploymentEntity.getInputProperties());
            deployRequest.setServiceRequestProperties(serviceRequestProperties);
        }
        return deployRequest;
    }

    /**
     * Method to create a ServiceConfigurationEntity from DeployTask.
     *
     * @param serviceDeploymentEntity DeployServiceEntity object.
     * @return ServiceConfigurationEntity.
     */
    public ServiceConfigurationEntity getInitialServiceConfiguration(
            ServiceDeploymentEntity serviceDeploymentEntity) {
        ServiceConfigurationEntity entity = new ServiceConfigurationEntity();
        entity.setServiceDeploymentEntity(serviceDeploymentEntity);
        entity.setCreatedTime(OffsetDateTime.now());
        entity.setUpdatedTime(OffsetDateTime.now());
        Map<String, Object> configuration = getServiceConfiguration(serviceDeploymentEntity);
        entity.setConfiguration(configuration);
        return entity;
    }

    private Map<String, Object> getServiceConfiguration(ServiceDeploymentEntity serviceDeployment) {
        Map<String, Object> configuration = new HashMap<>();
        DeployTask deployTask =
                getDeployTaskByStoredService(ServiceOrderType.DEPLOY, serviceDeployment);
        ServiceChangeManage serviceConfigurationManage =
                deployTask.getOcl().getServiceConfigurationManage();
        if (Objects.nonNull(serviceConfigurationManage)) {
            List<ServiceChangeParameter> configurationParameters =
                    serviceConfigurationManage.getConfigurationParameters();
            List<ServiceFlavorWithPrice> serviceFlavors =
                    deployTask.getOcl().getFlavors().getServiceFlavors();
            if (!CollectionUtils.isEmpty(configurationParameters)
                    && !CollectionUtils.isEmpty(serviceFlavors)) {
                Map<String, String> properties =
                        serviceFlavors.stream()
                                .filter(
                                        serviceFlavorWithPrice ->
                                                deployTask
                                                        .getDeployRequest()
                                                        .getFlavor()
                                                        .equals(serviceFlavorWithPrice.getName()))
                                .findFirst()
                                .map(ServiceFlavorWithPrice::getProperties)
                                .orElse(new HashMap<>());
                configurationParameters.forEach(
                        config -> {
                            String name = config.getName();
                            if (!properties.isEmpty() && properties.containsKey(name)) {
                                configuration.put(name, properties.get(name));
                            } else {
                                configuration.put(name, config.getInitialValue());
                            }
                        });
            }
        }
        return configuration;
    }
}
