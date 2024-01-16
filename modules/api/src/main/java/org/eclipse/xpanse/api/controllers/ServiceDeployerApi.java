/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;


import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.query.ServiceQueryModel;
import org.eclipse.xpanse.modules.models.service.view.DeployedService;
import org.eclipse.xpanse.modules.models.service.view.DeployedServiceDetails;
import org.eclipse.xpanse.modules.models.service.view.VendorHostedDeployedServiceDetails;
import org.eclipse.xpanse.modules.models.workflow.migrate.MigrateRequest;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployment;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.eclipse.xpanse.modules.workflow.consts.MigrateConstants;
import org.eclipse.xpanse.modules.workflow.utils.WorkflowProcessUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @Resource
    private DeployService deployService;

    @Resource
    private IdentityProviderManager identityProviderManager;

    @Resource
    private WorkflowProcessUtils workflowProcessUtils;

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
    public DeployedServiceDetails getSelfHostedServiceDetailsById(
            @Parameter(name = "id", description = "Task id of deployed service")
            @PathVariable("id") String id) {
        return this.deployService.getSelfHostedServiceDetailsByIdForEndUser(UUID.fromString(id));
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
    public VendorHostedDeployedServiceDetails getVendorHostedServiceDetailsById(
            @Parameter(name = "id", description = "Task id of deployed service")
            @PathVariable("id") String id) {
        return this.deployService.getVendorHostedServiceDetailsByIdForEndUser(UUID.fromString(id));
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
        ServiceQueryModel query =
                getServiceQueryModel(category, csp, serviceName, serviceVersion, serviceState);
        return this.deployService.listDeployedServices(query);
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
    public UUID deploy(@Valid @RequestBody DeployRequest deployRequest) {
        log.info("Starting managed service with name {}, version {}, csp {}",
                deployRequest.getServiceName(),
                deployRequest.getVersion(), deployRequest.getCsp());

        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        deployRequest.setUserId(userIdOptional.orElse(null));
        DeployTask deployTask = this.deployService.createNewDeployTask(deployRequest);
        Deployment deployment =
                this.deployService.getDeployment(deployTask.getOcl().getDeployment().getKind());
        this.deployService.deployService(deployment, deployTask);
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
     * Start a task to destroy the deployed service using id.
     *
     * @param id ID of deployed service.
     * @return response
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Start a task to destroy the deployed service using id.")
    @DeleteMapping(value = "/services/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Response destroy(@PathVariable("id") String id) {
        log.info("Stopping managed service with id {}", id);
        DeployServiceEntity deployServiceEntity =
                this.deployService.getDeployServiceEntity(UUID.fromString(id));
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        if (!StringUtils.equals(userIdOptional.orElse(null), deployServiceEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to destroy services belonging to other users.");
        }
        DeployTask destroyTask = this.deployService.getDestroyTask(deployServiceEntity);
        Deployment deployment =
                this.deployService.getDeployment(destroyTask.getOcl().getDeployment().getKind());
        this.deployService.destroyService(deployment, destroyTask, deployServiceEntity);
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
    public Response purge(@PathVariable("id") String id) {
        log.info("Purging managed service with id {}", id);
        DeployServiceEntity deployServiceEntity =
                this.deployService.getDeployServiceEntity(UUID.fromString(id));
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        if (!StringUtils.equals(userIdOptional.orElse(null), deployServiceEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to purge services belonging to other users.");
        }
        DeployTask purgeTask = this.deployService.getPurgeTask(deployServiceEntity);
        Deployment deployment =
                this.deployService.getDeployment(purgeTask.getOcl().getDeployment().getKind());
        this.deployService.asyncPurgeService(deployment, purgeTask, deployServiceEntity);
        String successMsg = String.format("Purging task for service with ID %s has started.", id);
        return Response.successResponse(Collections.singletonList(successMsg));
    }

    /**
     * Create a job to migrate the deployed service.
     *
     * @return response
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Create a job to migrate the deployed service.")
    @PostMapping(value = "/services/migration", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UUID migrate(@Valid @RequestBody MigrateRequest migrateRequest) {

        DeployServiceEntity deployServiceEntity =
                this.deployService.getDeployServiceEntity(migrateRequest.getId());
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        String userId = userIdOptional.orElse(null);
        if (!StringUtils.equals(userId, deployServiceEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to migrate services belonging to other users.");
        }
        UUID newId = UUID.randomUUID();
        Map<String, Object> variable = new HashMap<>();
        variable.put(MigrateConstants.ID, migrateRequest.getId());
        variable.put(MigrateConstants.NEW_ID, newId);
        variable.put(MigrateConstants.MIGRATE_REQUEST, migrateRequest);
        variable.put(MigrateConstants.USER_ID, userId);
        workflowProcessUtils.asyncStartProcess(MigrateConstants.PROCESS_KEY, variable);
        return newId;
    }

    private ServiceQueryModel getServiceQueryModel(Category category, Csp csp,
                                                   String serviceName,
                                                   String serviceVersion,
                                                   ServiceDeploymentState state) {
        ServiceQueryModel query = new ServiceQueryModel();
        if (Objects.nonNull(category)) {
            query.setCategory(category);
        }
        if (Objects.nonNull(csp)) {
            query.setCsp(csp);
        }
        if (StringUtils.isNotBlank(serviceName)) {
            query.setServiceName(serviceName);
        }
        if (StringUtils.isNotBlank(serviceVersion)) {
            query.setServiceVersion(serviceVersion);
        }
        if (Objects.nonNull(state)) {
            query.setServiceState(state);
        }
        return query;
    }
}
