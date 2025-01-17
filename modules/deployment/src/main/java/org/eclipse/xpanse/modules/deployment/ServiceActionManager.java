/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceChangeDetailsEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceChangeDetailsStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.logging.CustomRequestIdGenerator;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.serviceaction.ServiceActionRequest;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeParameter;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeScript;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Bean to manage service actions. */
@Slf4j
@Component
public class ServiceActionManager {

    @Resource private ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;

    @Resource private ServiceChangeDetailsStorage serviceChangeDetailsStorage;

    @Resource private ServiceTemplateStorage serviceTemplateStorage;

    @Resource private DeployService deployService;

    @Resource private ServiceChangeDetailsManager serviceChangeDetailsManager;

    @Resource
    private ServiceConfigurationVariablesJsonSchemaValidator
            serviceConfigurationVariablesJsonSchemaValidator;

    @Resource
    private ServiceConfigurationVariablesJsonSchemaGenerator
            serviceConfigurationVariablesJsonSchemaGenerator;

    /** create service action. */
    public ServiceOrder createServiceAction(UUID serviceId, ServiceActionRequest request) {
        try {
            ServiceDeploymentEntity serviceDeploymentEntity =
                    serviceDeploymentEntityHandler.getServiceDeploymentEntity(serviceId);
            ServiceTemplateEntity serviceTemplateEntity =
                    serviceTemplateStorage.getServiceTemplateById(
                            serviceDeploymentEntity.getServiceTemplateId());
            if (Objects.isNull(serviceTemplateEntity)) {
                String errMsg =
                        String.format(
                                "Service template with id %s not found.",
                                serviceDeploymentEntity.getServiceTemplateId());
                log.error(errMsg);
                throw new ServiceTemplateNotRegistered(errMsg);
            }
            validateServiceActions(serviceTemplateEntity, request.getActionParameters());
            UUID orderId = CustomRequestIdGenerator.generateOrderId();
            addServiceChangeDetailsForServiceActions(
                    orderId,
                    serviceId,
                    serviceDeploymentEntity,
                    serviceTemplateEntity.getOcl(),
                    request.getActionParameters(),
                    request.getActionName());
            return new ServiceOrder(orderId, serviceId);
        } catch (ServiceConfigurationInvalidException e) {
            String errorMsg =
                    String.format("Change service configuration error, %s", e.getErrorReasons());
            log.error(errorMsg);
            throw e;
        }
    }

    private void validateServiceActions(
            ServiceTemplateEntity serviceTemplateEntity,
            Map<String, Object> updateActionParameters) {
        List<ServiceChangeParameter> actionParameters =
                serviceTemplateEntity.getOcl().getServiceActions().stream()
                        .flatMap(action -> action.getActionParameters().stream())
                        .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(actionParameters)) {
            String errorMsg =
                    String.format(
                            "Service template %s has no service actions manage",
                            serviceTemplateEntity.getId());
            log.error(errorMsg);
            throw new ServiceConfigurationInvalidException(List.of(errorMsg));
        }

        JsonObjectSchema jsonObjectSchema =
                serviceConfigurationVariablesJsonSchemaGenerator
                        .buildServiceConfigurationJsonSchema(actionParameters);
        serviceConfigurationVariablesJsonSchemaValidator.validateServiceConfiguration(
                actionParameters, updateActionParameters, jsonObjectSchema);
    }

    private void addServiceChangeDetailsForServiceActions(
            UUID orderId,
            UUID serviceId,
            ServiceDeploymentEntity serviceDeployment,
            Ocl ocl,
            Map<String, Object> updateRequestMap,
            String actionName) {

        Map<String, List<DeployResource>> deployResourceMap =
                deployService
                        .listResourcesOfDeployedService(serviceId, DeployResourceKind.VM)
                        .stream()
                        .collect(Collectors.groupingBy(DeployResource::getGroupName));

        List<ServiceChangeScript> actionManageScripts =
                ocl.getServiceActions().stream()
                        .filter(serviceAction -> actionName.equals(serviceAction.getName()))
                        .flatMap(action -> action.getActionManageScripts().stream())
                        .toList();

        List<ServiceChangeParameter> actionParameters =
                ocl.getServiceActions().stream()
                        .filter(serviceAction -> actionName.equals(serviceAction.getName()))
                        .flatMap(action -> action.getActionParameters().stream())
                        .toList();
        List<ServiceChangeDetailsEntity> requests =
                serviceChangeDetailsManager.getAllServiceChangeDetails(
                        orderId,
                        serviceDeployment,
                        updateRequestMap,
                        deployResourceMap,
                        actionManageScripts,
                        actionParameters,
                        ServiceOrderType.SERVICE_ACTION);
        if (!CollectionUtils.isEmpty(requests)) {
            serviceChangeDetailsStorage.saveAll(requests);
        }
    }
}
