/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeRequestEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.enums.Handler;
import org.eclipse.xpanse.modules.models.service.enums.OrderStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.service.utils.ServiceConfigurationVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.serviceaction.ServiceActionRequest;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceActionChangeOrderAlreadyExistsException;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceAction;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeParameter;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeScript;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Bean to manage service actions. */
@Slf4j
@Component
public class ServiceActionManager {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource private ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;

    @Resource private DeployService deployService;

    @Resource private ServiceChangeRequestsManager serviceChangeRequestsManager;

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
            serviceDeploymentEntityHandler.validateServiceDeploymentStateForOrderType(
                    serviceDeploymentEntity, ServiceOrderType.SERVICE_ACTION);
            ServiceTemplateEntity serviceTemplateEntity =
                    serviceDeploymentEntity.getServiceTemplateEntity();
            validateAllActionChangeOrdersCompleted(serviceDeploymentEntity);
            validateServiceActions(serviceTemplateEntity, request.getActionParameters());
            UUID orderId =
                    addServiceChangeRequestsForServiceAction(
                            serviceId,
                            serviceDeploymentEntity,
                            serviceTemplateEntity.getOcl(),
                            request);
            return new ServiceOrder(orderId, serviceId);
        } catch (ServiceConfigurationInvalidException e) {
            String errorMsg =
                    String.format("Change service configuration error, %s", e.getErrorReasons());
            log.error(errorMsg);
            throw e;
        }
    }

    private void validateAllActionChangeOrdersCompleted(
            ServiceDeploymentEntity serviceDeploymentEntity) {
        List<ServiceOrderEntity> serviceOrders = serviceDeploymentEntity.getServiceOrders();
        if (CollectionUtils.isEmpty(serviceOrders)) {
            return;
        }
        List<ServiceOrderEntity> serviceActionOrders =
                serviceOrders.stream()
                        .filter(
                                serviceOrder ->
                                        serviceOrder.getTaskType()
                                                == ServiceOrderType.SERVICE_ACTION)
                        .toList();
        if (!CollectionUtils.isEmpty(serviceActionOrders)) {
            List<UUID> pendingOrderIds =
                    serviceActionOrders.stream()
                            .filter(
                                    order ->
                                            order.getOrderStatus() != OrderStatus.SUCCESSFUL
                                                    && order.getOrderStatus() != OrderStatus.FAILED)
                            .map(ServiceOrderEntity::getOrderId)
                            .toList();

            if (!CollectionUtils.isEmpty(pendingOrderIds)) {
                throw new ServiceActionChangeOrderAlreadyExistsException(
                        String.format(
                                "There is already a pending action change order. Order ID - %s",
                                pendingOrderIds.getFirst().toString()));
            }
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

    private UUID addServiceChangeRequestsForServiceAction(
            UUID serviceId,
            ServiceDeploymentEntity serviceDeployment,
            Ocl ocl,
            ServiceActionRequest serviceActionRequest) {
        Map<String, List<DeployResource>> deployResourceMap =
                deployService
                        .listResourcesOfDeployedService(serviceId, DeployResourceKind.VM)
                        .stream()
                        .collect(Collectors.groupingBy(DeployResource::getGroupName));

        // get scripts specific for the action name requested.
        List<ServiceChangeScript> actionManageScripts =
                ocl.getServiceActions().stream()
                        .filter(
                                serviceAction ->
                                        serviceActionRequest
                                                .getActionName()
                                                .equals(serviceAction.getName()))
                        .flatMap(action -> action.getActionManageScripts().stream())
                        .toList();

        return serviceChangeRequestsManager.createServiceOrderAndQueueServiceChangeRequests(
                serviceDeployment,
                serviceActionRequest,
                serviceActionRequest.getActionParameters(),
                // send the final request parameters by adding the missing ones with default values
                // from the service template.
                mergeRequestWithDefaultValuesForMissingParameters(
                        ocl,
                        serviceActionRequest.getActionParameters(),
                        serviceActionRequest.getActionName()),
                deployResourceMap,
                actionManageScripts,
                ServiceOrderType.SERVICE_ACTION,
                Handler.AGENT);
    }

    /**
     * Returns the specific service action management script from service template. This is called
     * when the agent requests for pending requests
     */
    public Optional<ServiceChangeScript> getServiceActionManageScript(
            ServiceChangeRequestEntity serviceChangeRequestEntity) {
        try {
            ServiceTemplateEntity serviceTemplateEntity =
                    serviceChangeRequestEntity
                            .getServiceDeploymentEntity()
                            .getServiceTemplateEntity();
            // get action name from the original request stored in the service order table.
            ServiceActionRequest serviceActionRequest =
                    objectMapper.readValue(
                            objectMapper.writeValueAsString(
                                    serviceChangeRequestEntity
                                            .getServiceOrderEntity()
                                            .getRequestBody()),
                            ServiceActionRequest.class);
            Optional<ServiceAction> serviceActionOptional =
                    serviceTemplateEntity.getOcl().getServiceActions().stream()
                            .filter(
                                    serviceAction ->
                                            serviceAction
                                                    .getName()
                                                    .equals(serviceActionRequest.getActionName()))
                            .findFirst();
            return serviceActionOptional.flatMap(
                    serviceAction ->
                            serviceAction.getActionManageScripts().stream()
                                    .filter(
                                            serviceChangeScript ->
                                                    serviceChangeScript
                                                            .getChangeHandler()
                                                            .equals(
                                                                    serviceChangeRequestEntity
                                                                            .getChangeHandler()))
                                    .findFirst());
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new ServiceConfigurationInvalidException(
                    List.of("Cannot process service action request body."));
        }
    }

    /**
     * update request parameters with missing parameters. The user can send only some parameters. We
     * fill the rest with default values provided in the service template.
     */
    private Map<String, Object> mergeRequestWithDefaultValuesForMissingParameters(
            Ocl ocl, Map<String, Object> updateRequestMap, String actionName) {
        Map<String, Object> updatedActionParametersRequest = new HashMap<>(updateRequestMap);
        ocl.getServiceActions().stream()
                .filter(serviceAction -> serviceAction.getName().equals(actionName))
                .findFirst()
                .ifPresent(
                        serviceAction ->
                                serviceAction
                                        .getActionParameters()
                                        .forEach(
                                                serviceChangeParameter -> {
                                                    if (!(updateRequestMap.containsKey(
                                                            serviceChangeParameter.getName()))) {
                                                        updatedActionParametersRequest.put(
                                                                serviceChangeParameter.getName(),
                                                                serviceChangeParameter.getValue());
                                                    }
                                                }));
        return updatedActionParametersRequest;
    }
}
