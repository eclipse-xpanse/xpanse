/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.auth.common.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.deployment.ServiceObjectManager;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.serviceobject.ServiceObjectDetails;
import org.eclipse.xpanse.modules.models.serviceobject.ServiceObjectRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Service Objects Api. */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/xpanse")
@Secured({ROLE_ADMIN, ROLE_USER})
@ConditionalOnProperty(name = "enable.agent.api.only", havingValue = "false", matchIfMissing = true)
public class ServiceObjectsApi {

    @Resource private ServiceObjectManager serviceObjectManager;

    /**
     * Get details of the managed service by serviceId.
     *
     * @return Details of the managed service.
     */
    @Tag(name = "ServiceObjects", description = "APIs for managing service's objects.")
    @Operation(description = "Get all objects of the service grouped by type.")
    @GetMapping(
            value = "/services/objects/{serviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceId", paramTypes = UUID.class)
    public Map<String, List<ServiceObjectDetails>> getObjectsByServiceId(
            @Parameter(name = "serviceId", description = "Id of the service")
                    @PathVariable("serviceId")
                    UUID serviceId) {
        return serviceObjectManager.getObjectsByServiceId(serviceId);
    }

    @Tag(name = "ServiceObjects", description = "APIs for managing service's objects.")
    @PostMapping(
            value = "/services/object/{serviceId}/{objectType}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(description = "Create an order to create object for the service.")
    @AuditApiRequest(methodName = "getCspFromServiceId", paramTypes = UUID.class)
    public ServiceOrder createServiceObject(
            @Parameter(name = "serviceId", description = "The id of the deployed service.")
                    @PathVariable("serviceId")
                    UUID serviceId,
            @Parameter(name = "objectType", description = "The action type to the service object.")
                    @PathVariable("objectType")
                    String objectType,
            @Valid @RequestBody ServiceObjectRequest request) {
        request.setObjectType(objectType);
        return serviceObjectManager.createOrderToCreateServiceObject(serviceId, request);
    }

    @Tag(name = "ServiceObjects", description = "APIs for managing service's objects.")
    @PutMapping(
            value = "/services/object/{serviceId}/{objectId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(description = "Create an order to update service object using object id.")
    @AuditApiRequest(methodName = "getCspFromServiceId", paramTypes = UUID.class)
    public ServiceOrder updateServiceObject(
            @Parameter(name = "serviceId", description = "The id of the deployed service.")
                    @PathVariable("serviceId")
                    UUID serviceId,
            @Parameter(name = "objectId", description = "The id of the service object.")
                    @PathVariable("objectId")
                    UUID objectId,
            @Valid @RequestBody ServiceObjectRequest request) {
        return serviceObjectManager.createOrderToUpdateServiceObject(serviceId, objectId, request);
    }

    @Tag(name = "ServiceObjects", description = "APIs for managing service's objects.")
    @DeleteMapping(
            value = "/services/object/{serviceId}/{objectId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(description = "Create an order to update service object using object id.")
    @AuditApiRequest(methodName = "getCspFromServiceId", paramTypes = UUID.class)
    public ServiceOrder deleteServiceObject(
            @Parameter(name = "serviceId", description = "The id of the deployed service.")
                    @PathVariable("serviceId")
                    UUID serviceId,
            @Parameter(name = "objectId", description = "The id of the service object.")
                    @PathVariable("objectId")
                    UUID objectId) {
        return serviceObjectManager.createOrderToDeleteServiceObject(serviceId, objectId);
    }
}
