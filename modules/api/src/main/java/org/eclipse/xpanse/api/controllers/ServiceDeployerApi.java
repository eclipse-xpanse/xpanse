/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;


import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_CSP;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ISV;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.api.config.OrderFailedApiResponses;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.deployment.ServiceDetailsViewManager;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.DeploymentStatusUpdate;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.modify.ModifyRequest;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.service.view.VendorHostedDeployedServiceDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;


/**
 * REST interface methods for processing OCL.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
@ConditionalOnProperty(name = "enable.agent.api.only", havingValue = "false", matchIfMissing = true)
public class ServiceDeployerApi {

    @Value("${region.azs.cache.expire.time.in.minutes:60}")
    private long duration;
    @Resource
    private DeployService deployService;
    @Resource
    private ServiceDetailsViewManager serviceDetailsViewManager;

    /**
     * Get details of the managed service by serviceId.
     *
     * @return Details of the managed service.
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description = "Get details of the deployed service by id.")
    @GetMapping(value = "/services/details/self_hosted/{serviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public DeployedServiceDetails getSelfHostedServiceDetailsById(
            @Parameter(name = "serviceId", description = "Id of the service")
            @PathVariable("serviceId") String serviceId) {
        return this.serviceDetailsViewManager.getSelfHostedServiceDetailsByIdForEndUser(
                UUID.fromString(serviceId));
    }


    /**
     * Get details of the managed vendor hosted service by serviceId.
     *
     * @return VendorHostedDeployedServiceDetails of the managed service.
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description = "Get deployed service details by serviceId.")
    @GetMapping(value = "/services/details/vendor_hosted/{serviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public VendorHostedDeployedServiceDetails getVendorHostedServiceDetailsById(
            @Parameter(name = "serviceId", description = "Id of the service")
            @PathVariable("serviceId") String serviceId) {
        return this.serviceDetailsViewManager.getVendorHostedServiceDetailsByIdForEndUser(
                UUID.fromString(serviceId));
    }

    /**
     * List all deployed services by a user.
     *
     * @return list of all services deployed by a user.
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description = "List all deployed services belongs to the user.")
    @GetMapping(value = "/services",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public List<DeployedService> listDeployedServices(
            @Parameter(name = "categoryName", description = "category of the service")
            @RequestParam(name = "categoryName", required = false) Category category,
            @Parameter(name = "cspName", description = "name of the cloud service provider")
            @RequestParam(name = "cspName", required = false) Csp csp,
            @Parameter(name = "serviceName", description = "name of the service")
            @RequestParam(name = "serviceName", required = false) String serviceName,
            @Parameter(name = "serviceVersion", description = "version of the service")
            @RequestParam(name = "serviceVersion", required = false) String serviceVersion,
            @Parameter(name = "serviceState", description = "deployment state of the service")
            @RequestParam(name = "serviceState", required = false)
            ServiceDeploymentState serviceState) {
        return this.serviceDetailsViewManager.listDeployedServices(
                category, csp, serviceName, serviceVersion, serviceState);
    }

    /**
     * List all deployed services details.
     *
     * @return list of all services deployed by a user.
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description = "List details of deployed services using parameters.")
    @GetMapping(value = "/services/details",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public List<DeployedService> listDeployedServicesDetails(
            @Parameter(name = "categoryName", description = "category of the service")
            @RequestParam(name = "categoryName", required = false) Category category,
            @Parameter(name = "cspName", description = "name of the cloud service provider")
            @RequestParam(name = "cspName", required = false) Csp csp,
            @Parameter(name = "serviceName", description = "name of the service")
            @RequestParam(name = "serviceName", required = false) String serviceName,
            @Parameter(name = "serviceVersion", description = "version of the service")
            @RequestParam(name = "serviceVersion", required = false) String serviceVersion,
            @Parameter(name = "serviceState", description = "deployment state of the service")
            @RequestParam(name = "serviceState", required = false)
            ServiceDeploymentState serviceState) {
        // return type is DeployedService but actually returns one of the child types
        // VendorHostedDeployedServiceDetails or DeployedServiceDetails
        return this.serviceDetailsViewManager.listDeployedServicesDetails(
                category, csp, serviceName, serviceVersion, serviceState);
    }

    /**
     * Create an order task to deploy new service using approved service template.
     *
     * @param deployRequest the request to deploy new service.
     * @return UUID
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description =
            "Create an order task to deploy new service using approved service template.")
    @PostMapping(value = "/services", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    @OrderFailedApiResponses
    public ServiceOrder deploy(@Valid @RequestBody DeployRequest deployRequest) {
        return this.deployService.createOrderToDeployNewService(deployRequest);
    }

    /**
     * Create an order task to redeploy the failed deployment using serviceId.
     *
     * @param serviceId ID of deployed service.
     * @return UUID
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description = "Create an order to redeploy the failed service using service id.")
    @PutMapping(value = "/services/deploy/retry/{serviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    @OrderFailedApiResponses
    public ServiceOrder redeployFailedDeployment(@PathVariable("serviceId") String serviceId) {
        return this.deployService.createOrderToRedeployFailedService(UUID.fromString(serviceId));
    }

    /**
     * Create an order task to modify the deployed service.
     *
     * @param modifyRequest the managed service to create.
     * @return UUID
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description = "Create an order task to modify the deployed service.")
    @PutMapping(value = "/services/modify/{serviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    @OrderFailedApiResponses
    public ServiceOrder modify(@Parameter(name = "serviceId", description = "Id of the service")
                               @PathVariable("serviceId") String serviceId,
                               @Valid @RequestBody ModifyRequest modifyRequest) {
        return this.deployService.createOrderToModifyDeployedService(
                UUID.fromString(serviceId), modifyRequest);
    }

    /**
     * Method to change lock configuration of the service.
     *
     * @param serviceLockConfig the lock config of the service.
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description = "Change the lock config of the service.")
    @PutMapping(value = "/services/changelock/{serviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public void changeServiceLockConfig(
            @Parameter(name = "serviceId", description = "Id of the service")
            @PathVariable("serviceId") String serviceId,
            @Valid @RequestBody ServiceLockConfig serviceLockConfig) {
        this.deployService.changeServiceLockConfig(UUID.fromString(serviceId), serviceLockConfig);
    }

    /**
     * Create an order task to destroy the deployed service using id.
     *
     * @param serviceId ID of deployed service.
     * @return UUID
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description = "Create an order task to destroy the deployed service using id.")
    @DeleteMapping(value = "/services/{serviceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    @OrderFailedApiResponses
    public ServiceOrder destroy(@Parameter(name = "serviceId", description = "Id of the service")
                                @PathVariable("serviceId") String serviceId) {
        return this.deployService.createOrderToDestroyDeployedService(UUID.fromString(serviceId));
    }

    /**
     * Create an order task to purge the deployed service using service id.
     *
     * @param serviceId ID of deployed service.
     * @return UUID
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description = "Create an order task to purge the deployed service using service id.")
    @DeleteMapping(value = "/services/purge/{serviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    @OrderFailedApiResponses
    public ServiceOrder purge(@Parameter(name = "serviceId", description = "Id of the service")
                              @PathVariable("serviceId") String serviceId) {
        return this.deployService.createOrderToPurgeDestroyedService(UUID.fromString(serviceId));
    }


    /**
     * Get details of the managed service by serviceId.
     *
     * @return Details of the managed service.
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description = "Get availability zones with csp and region.")
    @GetMapping(value = "/csp/region/azs",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Secured({ROLE_ADMIN, ROLE_CSP, ROLE_ISV, ROLE_USER})
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public ResponseEntity<List<String>> getAvailabilityZones(
            @Parameter(name = "cspName", description = "name of the cloud service provider")
            @RequestParam(name = "cspName") Csp csp,
            @Parameter(name = "siteName", description = "site of the region belongs to")
            @RequestParam(name = "siteName") String siteName,
            @Parameter(name = "regionName", description = "name of the region")
            @RequestParam(name = "regionName") String regionName,
            @Parameter(name = "serviceId", description = "Id of the deployed service")
            @RequestParam(name = "serviceId", required = false) String serviceId) {
        try {
            UUID serviceUuid =
                    StringUtils.isNotEmpty(serviceId) ? UUID.fromString(serviceId) : null;
            List<String> availabilityZones =
                    this.deployService.getAvailabilityZonesOfRegion(csp, siteName, regionName,
                            serviceUuid);
            return ResponseEntity.ok().cacheControl(getCacheControl()).body(availabilityZones);
        } catch (Exception ex) {
            log.error("Error fetching availability zones", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .cacheControl(CacheControl.noCache())
                    .body(Collections.singletonList(ex.getMessage()));
        }
    }

    /**
     * Method to fetch status of service deployment.
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @GetMapping(value = "/services/{serviceId}/deployment/status",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description =
            "Long-polling method to get the latest service deployment or service update status.")
    @AuditApiRequest(methodName = "getCspFromServiceId")
    @ResponseStatus(HttpStatus.OK)
    @Secured({ROLE_ADMIN, ROLE_ISV, ROLE_USER})
    public DeferredResult<DeploymentStatusUpdate> getLatestServiceDeploymentStatus(
            @Parameter(name = "serviceId", description = "ID of the service")
            @PathVariable(name = "serviceId") String serviceId,
            @Parameter(name = "lastKnownServiceDeploymentState",
                    description = "Last known service status to client. When provided, "
                            + "the service will wait for a configured period time until "
                            + "to see if there is a change to the last known state.")
            @RequestParam(name = "lastKnownServiceDeploymentState", required = false)
            ServiceDeploymentState lastKnownServiceDeploymentState) {
        return deployService.getLatestServiceDeploymentStatus(UUID.fromString(serviceId),
                lastKnownServiceDeploymentState);
    }


    /**
     * List compute resources of the service.
     *
     * @param serviceId ID of deployed service.
     * @return List of compute resources.
     */
    @Tag(name = "Service", description = "APIs to manage the services")
    @Operation(description = "List compute resources of the service.")
    @GetMapping(value = "/services/{serviceId}/resources/compute",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public List<DeployResource> getComputeResourceInventoryOfService(
            @Parameter(name = "serviceId", description = "Id of the deployed service")
            @PathVariable(name = "serviceId") String serviceId) {
        return this.deployService.listResourcesOfDeployedService(
                UUID.fromString(serviceId), DeployResourceKind.VM);
    }

    private CacheControl getCacheControl() {
        long durationTime = this.duration > 0 ? this.duration : 60;
        return CacheControl.maxAge(durationTime, TimeUnit.MINUTES).mustRevalidate();
    }
}
