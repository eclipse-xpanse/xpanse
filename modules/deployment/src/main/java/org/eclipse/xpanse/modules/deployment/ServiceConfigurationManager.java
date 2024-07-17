/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import static org.eclipse.xpanse.modules.logging.CustomRequestIdGenerator.TRACKING_ID;

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
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationUpdate;
import org.eclipse.xpanse.modules.models.serviceconfiguration.enums.ServiceConfigurationStatus;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceConfigurationParameter;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.slf4j.MDC;
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
        request.setOrderId(getOrderId());
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

        List<ServiceConfigurationParameter> configurationParameters =
                serviceTemplateEntity.getOcl().getConfigurationParameters();

        JsonObjectSchema jsonObjectSchema = serviceConfigurationVariablesJsonSchemaGenerator
                .buildServiceConfigurationJsonSchema(configurationParameters);

        serviceConfigurationVariablesJsonSchemaValidator.validateServiceConfiguration(
                configurationParameters, serviceConfigurationUpdate.getConfiguration(),
                jsonObjectSchema);
    }


    private UUID getOrderId() {
        UUID orderId = UUID.randomUUID();
        if (Objects.nonNull(MDC.get(TRACKING_ID))) {
            try {
                return UUID.fromString(MDC.get(TRACKING_ID));
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID string: {}", MDC.get(TRACKING_ID));
                return orderId;
            }
        } else {
            return orderId;
        }
    }
}
