/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.api.response.Response;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployTask;
import org.eclipse.xpanse.modules.deployment.Deployment;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.modules.ocl.loader.data.models.ServiceStatus;
import org.eclipse.xpanse.modules.ocl.loader.data.models.SystemStatus;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.Category;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.Csp;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.HealthStatus;
import org.eclipse.xpanse.modules.ocl.loader.data.models.query.RegisteredServiceQuery;
import org.eclipse.xpanse.modules.service.CreateRequest;
import org.eclipse.xpanse.orchestrator.OrchestratorService;
import org.eclipse.xpanse.orchestrator.register.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
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
public class OrchestratorApi {

    private final OrchestratorService orchestratorService;

    private final RegisterService registerService;

    @Autowired
    public OrchestratorApi(OrchestratorService orchestratorService,
            RegisterService registerService) {
        this.orchestratorService = orchestratorService;
        this.registerService = registerService;
    }

    /**
     * Register new service using ocl model.
     *
     * @param ocl model of Ocl.
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage register services.")
    @Operation(description = "Register new service using ocl model.")
    @PostMapping(value = "/register",
            consumes = {"application/x-yaml", "application/yml", "application/yaml"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response register(@Valid @RequestBody Ocl ocl) {
        registerService.registerService(ocl);
        String successMsg = String.format(
                "Registered new service with ocl %s.", ocl);
        log.info(successMsg);
        return Response.successResponse(successMsg);
    }

    /**
     * Update registered service using id and ocl model.
     *
     * @param ocl model of Ocl.
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage register services.")
    @Operation(description = "Update registered service using id and ocl model.")
    @PutMapping(value = "/register/{id}",
            consumes = {"application/x-yaml", "application/yml", "application/yaml"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response update(
            @Parameter(name = "id", description = "id of registered service")
            @PathVariable("id") String id, @Valid @RequestBody Ocl ocl) {
        log.info("Update registered service with id {}", id);
        registerService.updateRegisteredService(id, ocl);
        String successMsg = String.format(
                "Update registered service with id %s", id);
        log.info(successMsg);
        return Response.successResponse(successMsg);
    }

    /**
     * Register new service with URL of Ocl file.
     *
     * @param oclLocation URL of Ocl file.
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage register services.")
    @Operation(description = "Register new service with URL of Ocl file.")
    @PostMapping(value = "/register/file",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response fetch(@Parameter(name = "oclLocation", description = "URL of Ocl file")
                          @RequestParam(name = "oclLocation") String oclLocation)
            throws Exception {
        log.info("Register new service with Url {}", oclLocation);
        registerService.registerServiceByUrl(oclLocation);
        String successMsg = String.format(
                "Register new service with Url %s success.", oclLocation);
        log.info(successMsg);
        return Response.successResponse(successMsg);
    }


    /**
     * Update registered service using id and ocl file url.
     *
     * @param id          id of registered service.
     * @param oclLocation URL of new Ocl.
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage register services.")
    @Operation(description = "Update registered service using id and ocl file url.")
    @PutMapping(value = "/register/file/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response fetchUpdate(
            @Parameter(name = "id", description = "id of registered service")
            @PathVariable(name = "id") String id,
            @Parameter(name = "oclLocation", description = "URL of Ocl file")
            @RequestParam(name = "oclLocation") String oclLocation)
            throws Exception {
        log.info("Update registered service {} with Url {}", id, oclLocation);
        registerService.updateRegisteredServiceByUrl(id, oclLocation);
        String successMsg = String.format(
                "Update registered service %s with Url %s", id, oclLocation);
        log.info(successMsg);
        return Response.successResponse(successMsg);
    }

    /**
     * Unregister registered service using id.
     *
     * @param id id of registered service.
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage register services.")
    @Operation(description = "Unregister registered service using id.")
    @DeleteMapping("/register/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response unregister(
            @Parameter(name = "id", description = "id of registered service")
            @PathVariable("id") String id) {
        log.info("Unregister registered service using id {}", id);
        registerService.unregisterService(id);
        String successMsg = String.format(
                "Unregister registered service using id %s success.", id);
        log.info(successMsg);
        return Response.successResponse(successMsg);
    }


    /**
     * List registered service with query params.
     *
     * @param cspName     name of cloud service provider.
     * @param serviceName name of registered service.
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage register services.")
    @Operation(description = "List registered service with query params.")
    @GetMapping(value = "/register",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Response listRegisteredService(
            @Parameter(name = "categoryName", description = "category of the service")
            @RequestParam(name = "categoryName", required = false) String categoryName,
            @Parameter(name = "cspName", description = "name of the service provider")
            @RequestParam(name = "cspName", required = false) String cspName,
            @Parameter(name = "serviceName", description = "name of the service")
            @RequestParam(name = "serviceName", required = false) String serviceName,
            @Parameter(name = "serviceVersion", description = "version of the service")
            @RequestParam(name = "serviceVersion", required = false) String serviceVersion) {
        RegisteredServiceQuery query = new RegisteredServiceQuery();
        if (StringUtils.isNotBlank(cspName)) {
            query.setCsp(Csp.valueOf(cspName));
        }
        if (StringUtils.isNotBlank(categoryName)) {
            query.setCategory(Category.valueOf(categoryName));
        }
        query.setServiceName(serviceName);
        query.setServiceVersion(serviceVersion);
        log.info("List registered service with query model {}", query);
        List<RegisterServiceEntity> serviceEntities =
                registerService.queryRegisteredServices(query);
        String successMsg = String.format("List registered service with query model %s "
                + "success.", query);
        log.info(successMsg);
        return Response.successResponse(serviceEntities);
    }


    /**
     * Get registered service using id.
     *
     * @param id id of registered service.
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage register services.")
    @Operation(description = "Get registered service using id.")
    @GetMapping(value = "/register/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Response detail(
            @Parameter(name = "id", description = "id of registered service")
            @PathVariable("id") String id) {
        log.info("Get detail of registered service with name {}.", id);
        RegisterServiceEntity registerServiceEntity =
                registerService.getRegisteredService(id);
        String successMsg = String.format(
                "Get detail of registered service with name %s success.",
                id);
        log.info(successMsg);
        return Response.successResponse(registerServiceEntity.getOcl());
    }

    /**
     * Method to find out the current state of the system.
     *
     * @return Returns the current state of the system.
     */
    @Tag(name = "Admin", description = "APIs for administrating Xpanse")
    @GetMapping("/health")
    @ResponseStatus(HttpStatus.OK)
    public SystemStatus health() {
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        return systemStatus;
    }

    /**
     * Get status of the managed service with name.
     *
     * @return Status of the managed service.
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @GetMapping("/service")
    @ResponseStatus(HttpStatus.OK)
    public ServiceStatus state(@PathVariable("id") String id) {
        return null;
    }

    /**
     * Profiles the names of the managed services currently deployed.
     *
     * @return list of all services deployed.
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @GetMapping("/services")
    @ResponseStatus(HttpStatus.OK)
    public List<ServiceStatus> services() {
        return null;
    }

    /**
     * Start registered managed service.
     *
     * @param deployRequest the managed service to create.
     * @return response
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @PostMapping("/service")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Response start(@RequestBody CreateRequest deployRequest) {
        log.info("Starting managed service with name {}, version {}, csp {}",
                deployRequest.getName(),
                deployRequest.getVersion(), deployRequest.getCsp());
        DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.randomUUID());
        deployTask.setCreateRequest(deployRequest);
        Deployment deployment = this.orchestratorService.getDeployHandler(deployTask);
        this.orchestratorService.asyncDeployService(deployment, deployTask);
        String successMsg = String.format(
                "Task of start managed service %s-%s-%s start running.", deployRequest.getName(),
                deployRequest.getVersion(), deployRequest.getCsp());
        return Response.successResponse(successMsg);
    }

    /**
     * Stop started managed service.
     *
     * @param id name of managed service
     * @return response
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @DeleteMapping("/service/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Response stop(@PathVariable("id") String id) {
        log.info("Stopping managed service with id {}", id);
        DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.fromString(id));
        Deployment deployment = this.orchestratorService.getDestroyHandler(deployTask);
        this.orchestratorService.asyncDestroyService(deployment, deployTask);
        String successMsg = String.format(
                "Task of stop managed service %s start running.", id);
        return Response.successResponse(successMsg);
    }

}
