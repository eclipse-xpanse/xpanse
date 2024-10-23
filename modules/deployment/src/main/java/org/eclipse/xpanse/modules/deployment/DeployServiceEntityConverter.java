/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceConfigurationManage;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceConfigurationParameter;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavorWithPrice;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Bean to create DeployTask object from DeployServiceEntity. This is needed for everything other
 * than deploy tasks.
 */
@Component
public class DeployServiceEntityConverter {

    @Resource
    private ServiceTemplateStorage serviceTemplateStorage;

    /**
     * Method to create a DeployTask from DeployServiceEntity.
     *
     * @param deployServiceEntity DeployServiceEntity object.
     * @return DeployTask object.
     */
    public DeployTask getDeployTaskByStoredService(DeployServiceEntity deployServiceEntity) {
        // Set Ocl and CreateRequest
        DeployTask deployTask = new DeployTask();
        deployTask.setServiceId(deployServiceEntity.getId());
        deployTask.setUserId(deployServiceEntity.getUserId());
        deployTask.setDeployRequest(deployServiceEntity.getDeployRequest());
        ServiceTemplateEntity serviceTemplateEntity = serviceTemplateStorage.getServiceTemplateById(
                deployServiceEntity.getServiceTemplateId());
        deployTask.setOcl(serviceTemplateEntity.getOcl());
        deployTask.setServiceTemplateId(serviceTemplateEntity.getId());
        return deployTask;
    }

    /**
     * Method to create a ServiceConfigurationEntity from DeployTask.
     *
     * @param deployServiceEntity DeployServiceEntity object.
     * @return ServiceConfigurationEntity.
     */
    public ServiceConfigurationEntity getInitialServiceConfiguration(
            DeployServiceEntity deployServiceEntity) {
        ServiceConfigurationEntity entity = new ServiceConfigurationEntity();
        entity.setDeployServiceEntity(deployServiceEntity);
        entity.setCreatedTime(OffsetDateTime.now());
        Map<String, Object> configuration = getServiceConfiguration(deployServiceEntity);
        entity.setConfiguration(configuration);
        return entity;
    }

    private Map<String, Object> getServiceConfiguration(DeployServiceEntity deployServiceEntity) {
        Map<String, Object> configuration = new HashMap<>();
        DeployTask deployTask = getDeployTaskByStoredService(deployServiceEntity);
        ServiceConfigurationManage serviceConfigurationManage =
                deployTask.getOcl().getServiceConfigurationManage();
        if (Objects.nonNull(serviceConfigurationManage)) {
            List<ServiceConfigurationParameter> configurationParameters =
                    serviceConfigurationManage.getConfigurationParameters();
            List<ServiceFlavorWithPrice> serviceFlavors =
                    deployTask.getOcl().getFlavors().getServiceFlavors();
            if (!CollectionUtils.isEmpty(configurationParameters) && !CollectionUtils.isEmpty(
                    serviceFlavors)) {
                Map<String, String> properties =
                        serviceFlavors.stream()
                                .filter(serviceFlavorWithPrice -> deployTask.getDeployRequest()
                                        .getFlavor().equals(serviceFlavorWithPrice.getName()))
                                .findFirst().get().getProperties();
                configurationParameters.forEach(config -> {
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
