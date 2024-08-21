/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceConfigurationUpdateRequest;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceConfigurationUpdateStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.logging.CustomRequestIdGenerator;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationUpdate;
import org.eclipse.xpanse.modules.models.serviceconfiguration.enums.ServiceConfigurationStatus;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceConfigurationManage;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceConfigurationParameter;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Bean to manage service configuration.
 */
@Slf4j
@Component
public class ServiceConfigurationManager {

    @Resource
    private DeployServiceEntityHandler deployServiceEntityHandler;

    @Resource
    private ServiceConfigurationUpdateStorage serviceConfigurationUpdateStorage;

    @Resource
    private ServiceTemplateStorage serviceTemplateStorage;

    @Resource
    private ServiceConfigurationVariablesJsonSchemaValidator
            serviceConfigurationVariablesJsonSchemaValidator;

    @Resource
    private ServiceConfigurationVariablesJsonSchemaGenerator
            serviceConfigurationVariablesJsonSchemaGenerator;

    /**
     * update ServiceConfiguration.
     *
     * @param serviceId           The id of the deployed service.
     * @param configurationUpdate serviceConfigurationUpdate.
     */
    public ServiceOrder changeServiceConfiguration(String serviceId,
                                                   ServiceConfigurationUpdate configurationUpdate) {
        try {
            DeployServiceEntity deployServiceEntity =
                    deployServiceEntityHandler.getDeployServiceEntity(UUID.fromString(serviceId));
            validate(deployServiceEntity, configurationUpdate);
            Map<String, Object> existingConfiguration =
                    deployServiceEntity.getServiceConfigurationEntity().getConfiguration();
            Map<String, Object> updateRequestMap = configurationUpdate.getConfiguration();
            ServiceConfigurationUpdateRequest request =
                    addServiceConfigurationUpdateRequest(existingConfiguration, updateRequestMap);
            ServiceOrder order = new ServiceOrder();
            order.setOrderId(request.getOrderId());
            order.setServiceId(UUID.fromString(serviceId));
            return order;
        } catch (ServiceConfigurationInvalidException e) {
            String errorMsg =
                    String.format("Change service configuration error, %s ", e.getMessage());
            log.error(errorMsg);
            throw new ServiceConfigurationInvalidException(errorMsg);
        }
    }

    private ServiceConfigurationUpdateRequest addServiceConfigurationUpdateRequest(
            Map<String, Object> existingConfiguration, Map<String, Object> updateRequestMap) {
        ServiceConfigurationUpdateRequest request = new ServiceConfigurationUpdateRequest();
        request.setOrderId(CustomRequestIdGenerator.generateOrderId());
        request.setStatus(ServiceConfigurationStatus.PENDING);
        Map<String, Object> properties = new HashMap<>();
        properties.putAll(existingConfiguration);
        properties.putAll(updateRequestMap);
        request.setProperties(properties);
        return serviceConfigurationUpdateStorage.storeAndFlush(request);
    }

    private void validate(DeployServiceEntity deployServiceEntity,
                          ServiceConfigurationUpdate serviceConfigurationUpdate) {
        if (CollectionUtils.isEmpty(serviceConfigurationUpdate.getConfiguration())) {
            throw new IllegalArgumentException("Parameter ServiceConfigurationUpdate is empty");
        }
        ServiceTemplateEntity serviceTemplateEntity = serviceTemplateStorage.getServiceTemplateById(
                deployServiceEntity.getServiceTemplateId());
        if (Objects.isNull(serviceTemplateEntity)) {
            String errMsg = String.format("Service template with id %s not found.",
                    deployServiceEntity.getServiceTemplateId());
            log.error(errMsg);
            throw new ServiceTemplateNotRegistered(errMsg);
        }
        ServiceConfigurationManage serviceConfigurationManage =
                serviceTemplateEntity.getOcl().getServiceConfigurationManage();
        if (Objects.nonNull(serviceConfigurationManage)) {
            List<ServiceConfigurationParameter> configurationParameters = serviceConfigurationManage
                    .getConfigurationParameters();
            JsonObjectSchema jsonObjectSchema = serviceConfigurationVariablesJsonSchemaGenerator
                    .buildServiceConfigurationJsonSchema(configurationParameters);
            serviceConfigurationVariablesJsonSchemaValidator.validateServiceConfiguration(
                    configurationParameters, serviceConfigurationUpdate.getConfiguration(),
                    jsonObjectSchema);
        }

    }
}
