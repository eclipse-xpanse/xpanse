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
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.deployment.ServiceDetailsViewManager;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceLockedException;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.service.view.VendorHostedDeployedServiceDetails;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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


/**
 * REST interface methods for processing OCL.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
public class ServiceDeployerApi {

    @Value("${region.azs.cache.expire.time.in.minutes:60}")
    private long duration;

    @Resource
    private DeployService deployService;

    @Resource
    private UserServiceHelper userServiceHelper;

    @Resource
    private ServiceDetailsViewManager serviceDetailsViewManager;

    @Resource
    private DeployServiceEntityHandler deployServiceEntityHandler;

    @Resource
    private PluginManager pluginManager;

    /**
     * Get details of the managed service by id.
     *
     * @return Details of the managed service.
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Get deployed service details by id.")
    @GetMapping(value = "/services/details/self_hosted/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public DeployedServiceDetails getSelfHostedServiceDetailsById(
            @Parameter(name = "id", description = "Task id of deployed service")
            @PathVariable("id") String id) {
        return this.serviceDetailsViewManager.getSelfHostedServiceDetailsByIdForEndUser(
                UUID.fromString(id));
    }


    /**
     * Get details of the managed vendor hosted service by id.
     *
     * @return VendorHostedDeployedServiceDetails of the managed service.
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Get deployed service details by id.")
    @GetMapping(value = "/services/details/vendor_hosted/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public VendorHostedDeployedServiceDetails getVendorHostedServiceDetailsById(
            @Parameter(name = "id", description = "Task id of deployed service")
            @PathVariable("id") String id) {
        return this.serviceDetailsViewManager.getVendorHostedServiceDetailsByIdForEndUser(
                UUID.fromString(id));
    }

    /**
     * List all deployed services by a user.
     *
     * @return list of all services deployed by a user.
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "List all deployed services by a user.")
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
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "List all deployed services details.")
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
     * Start a task to deploy registered service.
     *
     * @param deployRequest the managed service to create.
     * @return response
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Start a task to deploy service using registered service template.")
    @PostMapping(value = "/services", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public UUID deploy(@Valid @RequestBody DeployRequest deployRequest) {
        log.info("Starting managed service with name {}, version {}, csp {}",
                deployRequest.getServiceName(),
                deployRequest.getVersion(), deployRequest.getCsp());

        String currentUserId = this.userServiceHelper.getCurrentUserId();
        deployRequest.setUserId(currentUserId);
        DeployTask deployTask = this.deployService.createNewDeployTask(deployRequest);
        this.deployService.deployService(deployTask);
        String successMsg = String.format(
                "Task for starting managed service %s-%s-%s-%s started. UUID %s",
                deployRequest.getServiceName(),
                deployRequest.getVersion(),
                deployRequest.getCsp().toValue(),
                deployRequest.getServiceHostingType().toValue(),
                deployTask.getId());
        log.info(successMsg);
        return deployTask.getId();
    }

    /**
     * Method to change lock configuration of the service.
     *
     * @param serviceLockConfig the lock config of the service.
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Change the lock config of the service.")
    @PutMapping(value = "/services/changelock/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public void changeServiceLockConfig(
            @Parameter(name = "id", description = "The id of the service")
            @PathVariable("id") String id,
            @Valid @RequestBody ServiceLockConfig serviceLockConfig) {
        DeployServiceEntity deployServiceEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(UUID.fromString(id));
        boolean currentUserIsOwner =
                this.userServiceHelper.currentUserIsOwner(deployServiceEntity.getUserId());
        if (!currentUserIsOwner) {
            throw new AccessDeniedException(
                    "No permissions to change lock config of services belonging to other users.");
        }
        deployService.changeServiceLockConfig(serviceLockConfig, deployServiceEntity);
        String successMsg = String.format(
                "Lock configuration of service %s updated.", id);
        log.info(successMsg);
    }

    /**
     * Start a task to destroy the deployed service using id.
     *
     * @param id ID of deployed service.
     * @return response
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Start a task to destroy the deployed service using id.")
    @DeleteMapping(value = "/services/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public Response destroy(@PathVariable("id") String id) {
        DeployServiceEntity deployServiceEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(UUID.fromString(id));
        boolean currentUserIsOwner =
                this.userServiceHelper.currentUserIsOwner(deployServiceEntity.getUserId());
        if (!currentUserIsOwner) {
            throw new AccessDeniedException(
                    "No permissions to destroy services belonging to other users.");
        }
        if (Objects.nonNull(deployServiceEntity.getLockConfig())
                && deployServiceEntity.getLockConfig().isDestroyLocked()) {
            String errorMsg = String.format("Service with id %s is locked from deletion.", id);
            throw new ServiceLockedException(errorMsg);
        }
        DeployTask destroyTask = this.deployService.getDestroyTask(deployServiceEntity);
        this.deployService.destroyService(destroyTask, deployServiceEntity);
        String successMsg = String.format(
                "Task for destroying managed service %s has started.", id);
        return Response.successResponse(Collections.singletonList(successMsg));
    }

    /**
     * Start a task to purge the deployed service using id.
     *
     * @param id ID of deployed service.
     * @return response
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Start a task to purge the deployed service using id.")
    @DeleteMapping(value = "/services/purge/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public Response purge(@PathVariable("id") String id) {
        log.info("Purging managed service with id {}", id);
        DeployServiceEntity deployServiceEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(UUID.fromString(id));
        boolean currentUserIsOwner = this.userServiceHelper.currentUserIsOwner(
                deployServiceEntity.getUserId());
        if (!currentUserIsOwner) {
            throw new AccessDeniedException(
                    "No permissions to purge services belonging to other users.");
        }
        DeployTask purgeTask = this.deployService.getPurgeTask(deployServiceEntity);
        this.deployService.purgeService(purgeTask, deployServiceEntity);
        String successMsg = String.format("Task for purging managed service %s has started.", id);
        return Response.successResponse(Collections.singletonList(successMsg));
    }


    /**
     * Start a task to redeploy the failed deployment using id.
     *
     * @param id ID of deployed service.
     * @return response
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Start a task to redeploy the failed deployment using id.")
    @PutMapping(value = "/services/deploy/retry/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public Response redeployFailedDeployment(@PathVariable("id") String id) {
        DeployServiceEntity deployServiceEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(UUID.fromString(id));
        boolean currentUserIsOwner = this.userServiceHelper.currentUserIsOwner(
                deployServiceEntity.getUserId());
        if (!currentUserIsOwner) {
            throw new AccessDeniedException(
                    "No permissions to redeploy services belonging to other users.");
        }
        this.deployService.redeployService(deployServiceEntity);
        String successMsg =
                String.format("Task for redeploying managed service %s has started.", id);
        return Response.successResponse(Collections.singletonList(successMsg));
    }


    /**
     * Get details of the managed service by id.
     *
     * @return Details of the managed service.
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Get availability zones with csp and region.")
    @GetMapping(value = "/csp/region/azs",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Secured({ROLE_ADMIN, ROLE_CSP, ROLE_ISV, ROLE_USER})
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public ResponseEntity<List<String>> getAvailabilityZones(
            @Parameter(name = "cspName", description = "name of the cloud service provider")
            @RequestParam(name = "cspName") Csp csp,
            @Parameter(name = "regionName", description = "name of the region")
            @RequestParam(name = "regionName") String regionName,
            @Parameter(name = "serviceId", description = "id of the deployed service")
            @RequestParam(name = "serviceId", required = false) String serviceId) {
        try {
            UUID uuid = StringUtils.isBlank(serviceId) ? null : UUID.fromString(serviceId);
            String currentUserId = this.userServiceHelper.getCurrentUserId();
            OrchestratorPlugin orchestratorPlugin = pluginManager.getOrchestratorPlugin(csp);
            List<String> availabilityZones = orchestratorPlugin.getAvailabilityZonesOfRegion(
                    currentUserId, regionName, uuid);
            return ResponseEntity.ok().cacheControl(getCacheControl()).body(availabilityZones);
        } catch (Exception ex) {
            log.error("Error fetching availability zones", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .cacheControl(CacheControl.noCache()).body(Collections.emptyList());
        }
    }

    private CacheControl getCacheControl() {
        long durationTime = this.duration > 0 ? this.duration : 60;
        return CacheControl.maxAge(durationTime, TimeUnit.MINUTES).mustRevalidate();
    }

}
