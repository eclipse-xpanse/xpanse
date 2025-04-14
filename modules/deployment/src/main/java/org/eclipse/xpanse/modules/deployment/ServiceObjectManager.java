/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeRequestEntity;
import org.eclipse.xpanse.modules.database.serviceobject.ServiceObjectEntity;
import org.eclipse.xpanse.modules.database.serviceobject.ServiceObjectStorage;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.utils.EntityTranslationUtils;
import org.eclipse.xpanse.modules.models.common.enums.UserOperation;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.Handler;
import org.eclipse.xpanse.modules.models.service.enums.OrderStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrderDetails;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.utils.ServiceObjectVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.service.utils.ServiceObjectVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.serviceconfiguration.exceptions.ServiceConfigurationInvalidException;
import org.eclipse.xpanse.modules.models.serviceobject.ServiceObjectDetails;
import org.eclipse.xpanse.modules.models.serviceobject.ServiceObjectRequest;
import org.eclipse.xpanse.modules.models.serviceobject.exceptions.ServiceObjectChangeOrderAlreadyExistsException;
import org.eclipse.xpanse.modules.models.serviceobject.exceptions.ServiceObjectNotFoundException;
import org.eclipse.xpanse.modules.models.serviceobject.exceptions.ServiceObjectRequestInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.ObjectManage;
import org.eclipse.xpanse.modules.models.servicetemplate.ObjectParameter;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceChangeScript;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceObject;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ObjectActionType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Bean to manage service objects. */
@Slf4j
@Component
public class ServiceObjectManager {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource private UserServiceHelper userServiceHelper;

    @Resource private ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;

    @Resource private DeployService deployService;

    @Resource private ServiceChangeRequestsManager serviceChangeRequestsManager;

    @Resource
    private ServiceObjectVariablesJsonSchemaValidator serviceObjectVariablesJsonSchemaValidator;

    @Resource
    private ServiceObjectVariablesJsonSchemaGenerator serviceObjectVariablesJsonSchemaGenerator;

    @Resource private ServiceObjectStorage serviceObjectStorage;

    @Resource private ServiceOrderStorage serviceOrderStorage;

    /**
     * Get service objects grouped by type with service id.
     *
     * @return Map of type and service objects.
     */
    public Map<String, List<ServiceObjectDetails>> getObjectsByServiceId(UUID serviceId) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                serviceDeploymentEntityHandler.getServiceDeploymentEntity(serviceId);
        checkPermission(serviceDeploymentEntity, UserOperation.VIEW_SERVICE_OBJECTS);
        List<ServiceObjectEntity> serviceObjectEntities =
                serviceObjectStorage.getObjectsByServiceId(serviceId);
        if (CollectionUtils.isEmpty(serviceObjectEntities)) {
            return Collections.emptyMap();
        }
        List<ServiceObjectDetails> serviceObjectDetails =
                convertToServiceObjectDetails(serviceObjectEntities);
        return serviceObjectDetails.stream()
                .collect(Collectors.groupingBy(ServiceObjectDetails::getObjectType));
    }

    private List<ServiceObjectDetails> convertToServiceObjectDetails(
            List<ServiceObjectEntity> serviceObjectEntities) {
        List<UUID> orderIds = new ArrayList<>();
        serviceObjectEntities.forEach(
                serviceObjectEntity -> {
                    orderIds.addAll(serviceObjectEntity.getObjectOrderIds());
                });
        List<ServiceOrderEntity> serviceOrderEntities =
                serviceOrderStorage.getEntitiesByIds(orderIds);
        Map<UUID, ServiceOrderEntity> serviceOrdersMap =
                serviceOrderEntities.stream()
                        .collect(
                                Collectors.toMap(
                                        ServiceOrderEntity::getOrderId,
                                        serviceOrder -> serviceOrder));
        List<ServiceObjectDetails> serviceObjectDetailsList = new ArrayList<>();
        serviceObjectEntities.forEach(
                serviceObjectEntity -> {
                    ServiceObjectDetails serviceObjectDetails =
                            EntityTranslationUtils.convertToServiceObjectDetails(
                                    serviceObjectEntity);
                    List<ServiceOrderDetails> serviceOrderDetailsList = new ArrayList<>();
                    serviceObjectEntity
                            .getObjectOrderIds()
                            .forEach(
                                    orderId -> {
                                        ServiceOrderDetails serviceOrderDetails =
                                                EntityTranslationUtils.transToServiceOrderDetails(
                                                        serviceOrdersMap.get(orderId));
                                        serviceOrderDetailsList.add(serviceOrderDetails);
                                    });
                    serviceOrderDetailsList.sort(
                            Comparator.comparing(ServiceOrderDetails::getStartedTime));
                    serviceObjectDetails.setObjectOrderHistory(serviceOrderDetailsList);
                    serviceObjectDetailsList.add(serviceObjectDetails);
                });
        return serviceObjectDetailsList;
    }

    /** create service action. */
    public ServiceOrder createOrderToCreateServiceObject(
            UUID serviceId, ServiceObjectRequest request) {
        try {
            ServiceDeploymentEntity serviceDeploymentEntity =
                    serviceDeploymentEntityHandler.getServiceDeploymentEntity(serviceId);
            checkPermission(serviceDeploymentEntity, UserOperation.CREATE_SERVICE_OBJECT);
            serviceDeploymentEntityHandler.validateServiceDeploymentStateForOrderType(
                    serviceDeploymentEntity, ServiceOrderType.OBJECT_CREATE);
            checkIfAnyOrderForSameObjectIsPending(
                    serviceDeploymentEntity.getServiceOrders(), request);
            ServiceTemplateEntity serviceTemplateEntity =
                    serviceDeploymentEntity.getServiceTemplateEntity();
            List<ServiceObject> serviceObjects = serviceTemplateEntity.getOcl().getServiceObjects();
            ObjectManage objectManageConfiguration =
                    findServiceObjectManageConfigurationByRequest(
                            serviceObjects, request, ObjectActionType.CREATE);
            validateServiceObjectRequestWithRequiredParameters(
                    request, objectManageConfiguration.getObjectParameters());
            return createServiceOrderForServiceObjectRequest(
                    serviceDeploymentEntity,
                    null,
                    request,
                    ServiceOrderType.OBJECT_CREATE,
                    objectManageConfiguration);
        } catch (ServiceConfigurationInvalidException e) {
            String errorMsg =
                    String.format("Change service configuration error, %s", e.getErrorReasons());
            log.error(errorMsg);
            throw e;
        }
    }

    /** Create order to update the service object. */
    public ServiceOrder createOrderToUpdateServiceObject(
            UUID serviceId, UUID objectId, ServiceObjectRequest request) {
        try {
            ServiceDeploymentEntity serviceDeploymentEntity =
                    serviceDeploymentEntityHandler.getServiceDeploymentEntity(serviceId);
            checkPermission(serviceDeploymentEntity, UserOperation.UPDATE_SERVICE_OBJECT);
            serviceDeploymentEntityHandler.validateServiceDeploymentStateForOrderType(
                    serviceDeploymentEntity, ServiceOrderType.OBJECT_MODIFY);
            ServiceObjectEntity serviceObjectEntity = serviceObjectStorage.getEntityById(objectId);
            if (serviceId != serviceObjectEntity.getServiceDeploymentEntity().getId()) {
                String errorMsg =
                        String.format(
                                "Request service object with id %s not belonging to the service"
                                        + " with id %s.",
                                objectId, serviceId);
                log.error(errorMsg);
                throw new ServiceObjectRequestInvalidException(List.of(errorMsg));
            }
            request.setObjectId(objectId);
            request.setObjectType(serviceObjectEntity.getObjectType());
            checkIfAnyOrderForSameObjectIsPending(
                    serviceDeploymentEntity.getServiceOrders(), request);
            // find service object configuration by object type and action type
            ServiceTemplateEntity serviceTemplateEntity =
                    serviceDeploymentEntity.getServiceTemplateEntity();
            List<ServiceObject> serviceObjectConfigurations =
                    serviceTemplateEntity.getOcl().getServiceObjects();
            ObjectManage objectManage =
                    findServiceObjectManageConfigurationByRequest(
                            serviceObjectConfigurations, request, ObjectActionType.UPDATE);
            validateServiceObjectRequestWithRequiredParameters(
                    request, objectManage.getObjectParameters());
            updateServiceObjectWithRequest(serviceObjectEntity, request);
            return createServiceOrderForServiceObjectRequest(
                    serviceDeploymentEntity,
                    serviceObjectEntity,
                    request,
                    ServiceOrderType.OBJECT_MODIFY,
                    objectManage);
        } catch (ServiceConfigurationInvalidException e) {
            String errorMsg =
                    String.format("Change service configuration error, %s", e.getErrorReasons());
            log.error(errorMsg);
            throw e;
        }
    }

    /** Create order to delete the service object. */
    public ServiceOrder createOrderToDeleteServiceObject(UUID serviceId, UUID objectId) {
        try {
            ServiceDeploymentEntity serviceDeploymentEntity =
                    serviceDeploymentEntityHandler.getServiceDeploymentEntity(serviceId);
            checkPermission(serviceDeploymentEntity, UserOperation.DELETE_SERVICE_OBJECT);
            serviceDeploymentEntityHandler.validateServiceDeploymentStateForOrderType(
                    serviceDeploymentEntity, ServiceOrderType.OBJECT_DELETE);
            ServiceObjectEntity serviceObjectEntity = serviceObjectStorage.getEntityById(objectId);
            if (serviceId != serviceObjectEntity.getServiceDeploymentEntity().getId()) {
                String errorMsg =
                        String.format(
                                "Request service object with id %s not belonging to the service"
                                        + " with id %s.",
                                objectId, serviceId);
                log.error(errorMsg);
                throw new ServiceObjectRequestInvalidException(List.of(errorMsg));
            }
            Set<UUID> objectIdsDependentOnObjectId =
                    serviceObjectStorage.getObjectIdsByDependentObjectId(objectId);
            if (!objectIdsDependentOnObjectId.isEmpty()) {
                String errorMsg =
                        String.format(
                                "Service object with id %s is the dependent of other service"
                                        + " objects with ids %s.",
                                objectId, objectIdsDependentOnObjectId);
                log.error(errorMsg);
                throw new ServiceObjectRequestInvalidException(List.of(errorMsg));
            }
            ServiceObjectRequest request = new ServiceObjectRequest();
            request.setObjectId(objectId);
            request.setObjectType(serviceObjectEntity.getObjectType());
            request.setObjectIdentifier(serviceObjectEntity.getObjectIdentifierName());
            checkIfAnyOrderForSameObjectIsPending(
                    serviceDeploymentEntity.getServiceOrders(), request);
            // find service object configuration by object type and action type
            ServiceTemplateEntity serviceTemplateEntity =
                    serviceDeploymentEntity.getServiceTemplateEntity();
            List<ServiceObject> serviceObjectConfigurations =
                    serviceTemplateEntity.getOcl().getServiceObjects();
            ObjectManage objectManageConfiguration =
                    findServiceObjectManageConfigurationByRequest(
                            serviceObjectConfigurations, request, ObjectActionType.DELETE);
            validateServiceObjectRequestWithRequiredParameters(
                    request, objectManageConfiguration.getObjectParameters());
            return createServiceOrderForServiceObjectRequest(
                    serviceDeploymentEntity,
                    serviceObjectEntity,
                    request,
                    ServiceOrderType.OBJECT_DELETE,
                    objectManageConfiguration);
        } catch (ServiceConfigurationInvalidException e) {
            String errorMsg =
                    String.format("Change service configuration error, %s", e.getErrorReasons());
            log.error(errorMsg);
            throw e;
        }
    }

    /**
     * Returns the specific service object management script from service template. This is called
     * when the agent requests for pending requests
     */
    public Optional<ServiceChangeScript> getServiceObjectManageScript(
            ServiceChangeRequestEntity serviceChangeRequestEntity) {
        try {
            ServiceTemplateEntity serviceTemplateEntity =
                    serviceChangeRequestEntity
                            .getServiceDeploymentEntity()
                            .getServiceTemplateEntity();
            // get object type from the original request stored in the service order table.
            ServiceObjectRequest serviceObjectRequest =
                    objectMapper.readValue(
                            objectMapper.writeValueAsString(
                                    serviceChangeRequestEntity
                                            .getServiceOrderEntity()
                                            .getRequestBody()),
                            ServiceObjectRequest.class);
            Optional<ServiceObject> serviceObjectOptional =
                    serviceTemplateEntity.getOcl().getServiceObjects().stream()
                            .filter(
                                    serviceObject ->
                                            serviceObject
                                                    .getType()
                                                    .equals(serviceObjectRequest.getObjectType()))
                            .findFirst();

            if (serviceObjectOptional.isEmpty()) {
                String errorMsg =
                        String.format(
                                "Service object with type %s not found in service template.",
                                serviceObjectRequest.getObjectType());
                log.error(errorMsg);
                throw new ServiceObjectRequestInvalidException(List.of(errorMsg));
            }

            ObjectActionType requestedActionType =
                    getRequestedActionType(serviceChangeRequestEntity);
            Optional<ObjectManage> objectManageOptional =
                    serviceObjectOptional.get().getObjectsManage().stream()
                            .filter(
                                    objectManage ->
                                            requestedActionType
                                                    == objectManage.getObjectActionType())
                            .findFirst();
            return objectManageOptional.map(ObjectManage::getObjectHandlerScript);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new ServiceObjectRequestInvalidException(
                    List.of("Cannot process service object request body."));
        }
    }

    /** updates object data after the configuration change request is processed successfully. */
    public void updateServiceObjectInDatabase(
            ServiceChangeRequestEntity request, ServiceOrderEntity serviceOrder) {
        try {
            log.info(
                    "Updating service object for service ID {}",
                    request.getServiceDeploymentEntity().getId());
            ServiceObjectRequest serviceObjectRequest =
                    objectMapper.readValue(
                            objectMapper.writeValueAsString(serviceOrder.getRequestBody()),
                            ServiceObjectRequest.class);
            ServiceTemplateEntity serviceTemplate =
                    request.getServiceDeploymentEntity().getServiceTemplateEntity();
            List<ServiceObject> serviceObjectConfigurations =
                    serviceTemplate.getOcl().getServiceObjects();
            ObjectActionType objectActionType = getRequestedActionType(request);
            ObjectManage objectManageConfiguration =
                    findServiceObjectManageConfigurationByRequest(
                            serviceObjectConfigurations, serviceObjectRequest, objectActionType);
            maskSecretParametersInDatabase(
                    objectManageConfiguration.getObjectParameters(),
                    serviceObjectRequest,
                    serviceOrder);
            if (ServiceOrderType.OBJECT_CREATE == serviceOrder.getTaskType()) {
                addServiceObjectEntity(serviceOrder, serviceObjectRequest);
                log.info(
                        "Service object created for service ID {}",
                        request.getServiceDeploymentEntity().getId());
            } else if (ServiceOrderType.OBJECT_MODIFY == serviceOrder.getTaskType()) {
                updateServiceObjectEntity(serviceOrder, serviceObjectRequest);
                log.info(
                        "Updated service object {} for service ID {}",
                        serviceObjectRequest.getObjectId(),
                        request.getServiceDeploymentEntity().getId());
            } else if (ServiceOrderType.OBJECT_DELETE == serviceOrder.getTaskType()) {
                deleteServiceObjectEntity(serviceOrder, serviceObjectRequest.getObjectId());
                log.info(
                        "Deleted service object {} for service ID {}",
                        serviceObjectRequest.getObjectId(),
                        request.getServiceDeploymentEntity().getId());
            }
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void maskSecretParametersInDatabase(
            List<ObjectParameter> objectParameters,
            ServiceObjectRequest serviceObjectRequest,
            ServiceOrderEntity serviceOrder) {
        Set<String> secretParameters =
                objectParameters.stream()
                        .filter(parameter -> parameter.getSensitiveScope() != SensitiveScope.NONE)
                        .map(ObjectParameter::getName)
                        .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(secretParameters)) {
            return;
        }

        if (!CollectionUtils.isEmpty(serviceObjectRequest.getServiceObjectParameters())) {
            serviceObjectRequest
                    .getServiceObjectParameters()
                    .forEach(
                            (key, value) -> {
                                if (secretParameters.contains(key)) {
                                    serviceObjectRequest
                                            .getServiceObjectParameters()
                                            .put(key, SensitiveDataHandler.SENSITIVE_VALUE);
                                }
                            });
        }

        if (Objects.nonNull(serviceOrder.getRequestBody())) {
            serviceOrder
                    .getRequestBody()
                    .put(
                            "serviceObjectParameters",
                            serviceObjectRequest.getServiceObjectParameters());
        }
    }

    private void addServiceObjectEntity(
            ServiceOrderEntity serviceOrder, ServiceObjectRequest request) {
        ServiceObjectEntity serviceObjectEntity = new ServiceObjectEntity();
        serviceObjectEntity.setServiceDeploymentEntity(serviceOrder.getServiceDeploymentEntity());
        serviceObjectEntity.setObjectType(request.getObjectType());
        serviceObjectEntity.setObjectIdentifierName(request.getObjectIdentifier());
        serviceObjectEntity.setProperties(request.getServiceObjectParameters());
        if (Objects.nonNull(request.getLinkedObjects())) {
            serviceObjectEntity.setDependentObjectIds(new HashSet<>(request.getLinkedObjects()));
        }
        serviceObjectEntity.setObjectOrderIds(Set.of(serviceOrder.getOrderId()));
        serviceObjectStorage.storeAndFlush(serviceObjectEntity);
    }

    private void updateServiceObjectEntity(
            ServiceOrderEntity serviceOrder, ServiceObjectRequest request) {
        ServiceObjectEntity serviceObjectEntity =
                serviceObjectStorage.getEntityById(request.getObjectId());
        serviceObjectEntity.setObjectType(request.getObjectType());
        serviceObjectEntity.setObjectIdentifierName(request.getObjectIdentifier());
        serviceObjectEntity.setProperties(request.getServiceObjectParameters());
        if (Objects.nonNull(request.getLinkedObjects())) {
            serviceObjectEntity.setDependentObjectIds(new HashSet<>(request.getLinkedObjects()));
        }
        serviceObjectEntity.getObjectOrderIds().add(serviceOrder.getOrderId());
        serviceObjectStorage.storeAndFlush(serviceObjectEntity);
    }

    private void deleteServiceObjectEntity(ServiceOrderEntity serviceOrder, UUID objectId) {
        ServiceObjectEntity serviceObjectEntity = serviceObjectStorage.getEntityById(objectId);
        serviceObjectEntity.getObjectOrderIds().add(serviceOrder.getOrderId());
        serviceObjectStorage.delete(serviceObjectEntity);
    }

    private ObjectActionType getRequestedActionType(
            ServiceChangeRequestEntity serviceChangeRequestEntity) {
        ServiceOrderType serviceOrderType =
                serviceChangeRequestEntity.getServiceOrderEntity().getTaskType();
        return switch (serviceOrderType) {
            case OBJECT_CREATE -> ObjectActionType.CREATE;
            case OBJECT_MODIFY -> ObjectActionType.UPDATE;
            case OBJECT_DELETE -> ObjectActionType.DELETE;
            default ->
                    throw new UnsupportedEnumValueException(
                            String.format(
                                    "ServiceOrderType value %s is not supported.",
                                    serviceOrderType));
        };
    }

    /**
     * Find matched service object configuration in service template by request.
     *
     * @return matched service object configuration.
     */
    private ObjectManage findServiceObjectManageConfigurationByRequest(
            List<ServiceObject> serviceObjects,
            ServiceObjectRequest request,
            ObjectActionType objectActionType) {
        Optional<ServiceObject> serviceObjectOptional =
                serviceObjects.stream()
                        .filter(
                                serviceObject ->
                                        StringUtils.equals(
                                                request.getObjectType(), serviceObject.getType()))
                        .findFirst();
        if (serviceObjectOptional.isEmpty()) {
            String errMsg =
                    String.format(
                            "Could not find any service object configuration with object type %s"
                                    + " for the service",
                            request.getObjectType());
            log.error(errMsg);
            throw new ServiceObjectRequestInvalidException(List.of(errMsg));
        }
        Optional<ObjectManage> objectManageOptional =
                serviceObjectOptional.get().getObjectsManage().stream()
                        .filter(
                                objectManage ->
                                        objectActionType == objectManage.getObjectActionType())
                        .findAny();
        if (objectManageOptional.isEmpty()) {
            String errMsg =
                    String.format(
                            "Could not find any defined service object manage configuration with"
                                    + " action type %s for the service",
                            objectActionType);
            log.error(errMsg);
            throw new ServiceObjectRequestInvalidException(List.of(errMsg));
        }
        return objectManageOptional.get();
    }

    /** Validate service object request with required parameters. */
    private void validateServiceObjectRequestWithRequiredParameters(
            ServiceObjectRequest request, List<ObjectParameter> objectParameters) {
        if (Objects.isNull(request.getServiceObjectParameters()) || objectParameters.isEmpty()) {
            return;
        }
        JsonObjectSchema jsonObjectSchema =
                serviceObjectVariablesJsonSchemaGenerator.buildJsonSchemaOfServiceObjectParameters(
                        objectParameters);
        serviceObjectVariablesJsonSchemaValidator.validateServiceObjectParameters(
                request.getServiceObjectParameters(), jsonObjectSchema);

        checkLinkedObjects(request);
    }

    private void checkPermission(
            ServiceDeploymentEntity deployedService, UserOperation userOperation) {
        boolean currentUserIsOwner =
                userServiceHelper.currentUserIsOwner(deployedService.getUserId());
        if (!currentUserIsOwner) {
            String errorMsg =
                    String.format(
                            "No permission to %s owned by other users.", userOperation.toValue());
            log.error(errorMsg);
            throw new AccessDeniedException(errorMsg);
        }
    }

    private void checkIfAnyOrderForSameObjectIsPending(
            List<ServiceOrderEntity> serviceOrders, ServiceObjectRequest request) {
        List<UUID> pendingOrderIds =
                serviceOrders.stream()
                        .filter(serviceOrder -> isPendingOrderForSameObject(serviceOrder, request))
                        .map(ServiceOrderEntity::getOrderId)
                        .toList();
        if (!CollectionUtils.isEmpty(pendingOrderIds)) {
            throw new ServiceObjectChangeOrderAlreadyExistsException(
                    String.format(
                            "There is already a pending Object manage order. Order ID - %s",
                            pendingOrderIds.getFirst().toString()));
        }
    }

    private void checkLinkedObjects(ServiceObjectRequest request) {
        if (CollectionUtils.isEmpty(request.getLinkedObjects())) {
            return;
        }
        List<ServiceObjectEntity> linkedObjects =
                serviceObjectStorage.getEntitiesByIds(request.getLinkedObjects());
        List<UUID> existingObjectIds =
                linkedObjects.stream().map(ServiceObjectEntity::getObjectId).toList();
        Set<UUID> notFoundObjectIds = new HashSet<>();
        for (UUID linkedObjectId : request.getLinkedObjects()) {
            if (!existingObjectIds.contains(linkedObjectId)) {
                notFoundObjectIds.add(linkedObjectId);
            }
        }
        if (!CollectionUtils.isEmpty(notFoundObjectIds)) {
            String errMsg =
                    String.format(
                            "Could not find service objects with linked object ids %s",
                            notFoundObjectIds);
            log.error(errMsg);
            throw new ServiceObjectNotFoundException(errMsg);
        }
    }

    private boolean isPendingOrderForSameObject(
            ServiceOrderEntity serviceOrder, ServiceObjectRequest request) {
        return (ServiceOrderType.OBJECT_CREATE == serviceOrder.getTaskType()
                        || ServiceOrderType.OBJECT_MODIFY == serviceOrder.getTaskType()
                        || ServiceOrderType.OBJECT_DELETE == serviceOrder.getTaskType())
                && (OrderStatus.CREATED == serviceOrder.getOrderStatus()
                        || OrderStatus.IN_PROGRESS == serviceOrder.getOrderStatus())
                && orderHasSameObjectTypeAndIdentifierWithRequest(serviceOrder, request);
    }

    private boolean orderHasSameObjectTypeAndIdentifierWithRequest(
            ServiceOrderEntity serviceOrder, ServiceObjectRequest request) {
        ServiceObjectRequest storedServiceObjectRequest =
                objectMapper.convertValue(
                        serviceOrder.getRequestBody(), ServiceObjectRequest.class);
        return StringUtils.equals(
                        storedServiceObjectRequest.getObjectType(), request.getObjectType())
                && StringUtils.equals(
                        storedServiceObjectRequest.getObjectIdentifier(),
                        request.getObjectIdentifier());
    }

    private void updateServiceObjectWithRequest(
            ServiceObjectEntity serviceObjectEntity, ServiceObjectRequest serviceObjectRequest) {
        if (!CollectionUtils.isEmpty(serviceObjectRequest.getLinkedObjects())) {
            serviceObjectEntity.setDependentObjectIds(
                    new HashSet<>(serviceObjectRequest.getLinkedObjects()));
            serviceObjectStorage.storeAndFlush(serviceObjectEntity);
        }
    }

    private ServiceOrder createServiceOrderForServiceObjectRequest(
            ServiceDeploymentEntity serviceDeployment,
            ServiceObjectEntity serviceObjectEntity,
            ServiceObjectRequest serviceObjectRequest,
            ServiceOrderType orderType,
            ObjectManage objectManage) {
        Map<String, List<DeployResource>> deployResourceMap =
                deployService
                        .listResourcesOfDeployedService(serviceDeployment.getId(), null)
                        .stream()
                        .collect(Collectors.groupingBy(DeployResource::getGroupName));
        ServiceChangeScript serviceChangeScript = objectManage.getObjectHandlerScript();
        List<ObjectParameter> objectParameters = objectManage.getObjectParameters();
        Map<String, Object> originalProperties = serviceObjectRequest.getServiceObjectParameters();
        Map<String, Object> finalPropertiesToBeUsed =
                fillMissingParametersWithStoredPropertiesOrDefaultValues(
                        originalProperties, serviceObjectEntity, objectParameters);
        UUID orderId =
                serviceChangeRequestsManager.createServiceOrderAndQueueServiceChangeRequests(
                        serviceDeployment,
                        serviceObjectRequest,
                        serviceObjectRequest.getServiceObjectParameters(),
                        finalPropertiesToBeUsed,
                        deployResourceMap,
                        List.of(serviceChangeScript),
                        orderType,
                        Handler.AGENT);
        return new ServiceOrder(orderId, serviceDeployment.getId());
    }

    /**
     * Fill missing parameters in request with stored properties in object or default values in
     * configuration.
     */
    private Map<String, Object> fillMissingParametersWithStoredPropertiesOrDefaultValues(
            Map<String, Object> requestMap,
            ServiceObjectEntity serviceObjectEntity,
            List<ObjectParameter> objectParameters) {
        Map<String, Object> updatedParametersRequest = new HashMap<>();
        if (!CollectionUtils.isEmpty(requestMap)) {
            updatedParametersRequest.putAll(requestMap);
        }
        Set<String> missingRequiredParameters =
                objectParameters.stream()
                        .filter(
                                objectParameter ->
                                        objectParameter.getIsMandatory()
                                                && !updatedParametersRequest.containsKey(
                                                        objectParameter.getName()))
                        .map(ObjectParameter::getName)
                        .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(missingRequiredParameters)) {
            return updatedParametersRequest;
        }
        Map<String, Object> storedProperties = new HashMap<>();
        if (Objects.nonNull(serviceObjectEntity)
                && !CollectionUtils.isEmpty(serviceObjectEntity.getProperties())) {
            storedProperties.putAll(serviceObjectEntity.getProperties());
        }
        for (String requiredParameter : missingRequiredParameters) {
            if (!updatedParametersRequest.containsKey(requiredParameter)) {
                // fill with stored properties if exist
                if (storedProperties.containsKey(requiredParameter)) {
                    updatedParametersRequest.put(
                            requiredParameter, storedProperties.get(requiredParameter));
                    log.info(
                            "The parameter {} is missing in request, use the stored value {}.",
                            requiredParameter,
                            serviceObjectEntity.getProperties().get(requiredParameter));

                } else {
                    Optional<ObjectParameter> objectParameterOptional =
                            objectParameters.stream()
                                    .filter(
                                            objectParameter ->
                                                    objectParameter
                                                            .getName()
                                                            .equals(requiredParameter))
                                    .findFirst();
                    if (objectParameterOptional.isPresent()) {
                        ObjectParameter objectParameter = objectParameterOptional.get();
                        updatedParametersRequest.put(
                                requiredParameter, objectParameter.getExample());
                        log.info(
                                "The parameter {} is missing in request, use the default value {}.",
                                requiredParameter,
                                objectParameter.getExample());
                    }
                }
            }
        }
        return updatedParametersRequest;
    }
}
