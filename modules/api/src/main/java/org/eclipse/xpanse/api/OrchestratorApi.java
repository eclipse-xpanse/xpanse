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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.api.response.Response;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.deployment.Deployment;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.DeployTask;
import org.eclipse.xpanse.modules.models.SystemStatus;
import org.eclipse.xpanse.modules.models.enums.Category;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.HealthStatus;
import org.eclipse.xpanse.modules.models.query.RegisteredServiceQuery;
import org.eclipse.xpanse.modules.models.resource.Ocl;
import org.eclipse.xpanse.modules.models.service.BillingDataResponse;
import org.eclipse.xpanse.modules.models.service.CreateRequest;
import org.eclipse.xpanse.modules.models.service.MonitorResource;
import org.eclipse.xpanse.modules.models.view.CategoryOclVo;
import org.eclipse.xpanse.modules.models.view.OclDetailVo;
import org.eclipse.xpanse.modules.models.view.ServiceDetailVo;
import org.eclipse.xpanse.modules.models.view.ServiceVo;
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
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage register services.")
    @Operation(description = "Get category list.")
    @GetMapping(value = "/register/categories",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<Category> listCategories() {
        return Arrays.asList(Category.values());
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
    public UUID register(@Valid @RequestBody Ocl ocl) {
        UUID uuid = registerService.registerService(ocl);
        String successMsg = String.format(
                "Registered new service success. uuid %s", uuid);
        log.info(successMsg);
        return uuid;
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
                "Update registered service with id %s success.", id);
        log.info(successMsg);
        this.orchestratorService.updateOpenApi(id);
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
    public UUID fetch(
            @Parameter(name = "oclLocation", description = "URL of Ocl file")
            @RequestParam(name = "oclLocation") String oclLocation)
            throws Exception {
        log.info("Register new service with Url {}", oclLocation);
        UUID uuid = registerService.registerServiceByUrl(oclLocation);
        String successMsg = String.format(
                "Register new service with Url %s success.UUID: %s", oclLocation, uuid);
        log.info(successMsg);
        return uuid;
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
        this.orchestratorService.updateOpenApi(id);
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
        this.orchestratorService.deleteOpenApi(id);
        return Response.successResponse(successMsg);
    }


    /**
     * List registered service with query params.
     *
     * @param categoryName   name of category.
     * @param cspName        name of cloud service provider.
     * @param serviceName    name of registered service.
     * @param serviceVersion version of registered service.
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage register services.")
    @Operation(description = "List registered service with query params.")
    @GetMapping(value = "/register",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<RegisterServiceEntity> listRegisteredServices(
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
            query.setCsp(Csp.valueOf(StringUtils.upperCase(cspName)));
        }
        if (StringUtils.isNotBlank(categoryName)) {
            query.setCategory(Category.valueOf(StringUtils.upperCase(categoryName)));
        }
        query.setServiceName(serviceName);
        query.setServiceVersion(serviceVersion);
        log.info("List registered service with query model {}", query);
        List<RegisterServiceEntity> serviceEntities =
                registerService.queryRegisteredServices(query);
        String successMsg = String.format("List registered service with query model %s "
                + "success.", query);
        log.info(successMsg);
        return serviceEntities;
    }


    /**
     * List registered service with category.
     *
     * @param categoryName name of category.
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage register services.")
    @Operation(description = "List registered service group by serviceName, serviceVersion, "
            + "cspName with category.")
    @GetMapping(value = "/register/category/{categoryName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryOclVo> listRegisteredServicesTree(
            @Parameter(name = "categoryName", description = "category of the service")
            @PathVariable(name = "categoryName", required = false) String categoryName) {
        Category category = Category.valueOf(StringUtils.upperCase(categoryName));
        RegisteredServiceQuery query = new RegisteredServiceQuery();
        query.setCategory(category);
        log.info("List registered service with query model {}", query);
        List<CategoryOclVo> categoryOclList =
                registerService.queryRegisteredServicesTree(query);
        String successMsg = String.format("List registered service with query model %s "
                + "success.", query);
        log.info(successMsg);
        return categoryOclList;
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
    public OclDetailVo detail(
            @Parameter(name = "id", description = "id of registered service")
            @PathVariable("id") String id) {
        log.info("Get detail of registered service with name {}.", id);
        OclDetailVo oclDetailVo = registerService.getRegisteredService(id);
        String successMsg = String.format(
                "Get detail of registered service with name %s success.",
                id);
        log.info(successMsg);
        return oclDetailVo;
    }

    /**
     * Method to find out the current state of the system.
     *
     * @return Returns the current state of the system.
     */
    @Tag(name = "Admin", description = "APIs for administrating Xpanse")
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
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
    @Operation(description = "Get deployed service using id.")
    @GetMapping(value = "/service/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ServiceDetailVo serviceDetail(
            @Parameter(name = "id", description = "Task id of deploy service")
            @PathVariable("id") String id) {

        return this.orchestratorService.getDeployServiceDetail(UUID.fromString(id));
    }

    /**
     * List the deployed services.
     *
     * @return list of all services deployed.
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "List the deployed services.")
    @GetMapping(value = "/services", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<ServiceVo> services() {
        return this.orchestratorService.listDeployServices();
    }

    /**
     * Get monitor data.
     *
     * @param id deploy service UUID.
     * @return response
     */
    @Tag(name = "Service", description = "APIs to get monitor data")
    @GetMapping(value = "/service/monitor/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public MonitorResource monitor(@PathVariable("id") UUID id,
            @Parameter(name = "fromTime", description = "the start time of the monitoring range")
            @RequestParam(value = "fromTime", required = false) String fromTime,
            @Parameter(name = "toTime", description = "the end time of the monitoring range")
            @RequestParam(value = "toTime", required = false) String toTime) {

        return this.orchestratorService.monitor(id, fromTime, toTime);
    }

    /**
     * Get billing data.
     *
     * @param id deploy service UUID.
     * @return response
     */
    @Tag(name = "Service", description = "APIs to get billing data")
    @GetMapping(value = "/service/billing/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<BillingDataResponse> billing(@PathVariable("id") UUID id,
            @Parameter(name = "unit", description = "the unit of the unit price")
            @RequestParam(value = "unit", required = false) Boolean unit) {

        return this.orchestratorService.billing(id, unit);
    }

    /**
     * Start registered managed service.
     *
     * @param deployRequest the managed service to create.
     * @return response
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Start a task to deploy registered service.")
    @PostMapping(value = "/service", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UUID deploy(@Valid @RequestBody CreateRequest deployRequest) {
        log.info("Starting managed service with name {}, version {}, csp {}",
                deployRequest.getName(),
                deployRequest.getVersion(), deployRequest.getCsp());
        UUID id = UUID.randomUUID();
        DeployTask deployTask = new DeployTask();
        deployRequest.setId(id);
        deployTask.setId(id);
        deployTask.setCreateRequest(deployRequest);
        Deployment deployment = this.orchestratorService.getDeployHandler(deployTask);
        this.orchestratorService.asyncDeployService(deployment, deployTask);
        String successMsg = String.format(
                "Task of start managed service %s-%s-%s start running. UUID %s",
                deployRequest.getName(),
                deployRequest.getVersion(), deployRequest.getCsp(), deployTask.getId());
        log.info(successMsg);
        return id;
    }

    /**
     * Start a task to destroy the deployed service using id.
     *
     * @param id ID of deployed service.
     * @return response
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Start a task to destroy the deployed service using id.")
    @DeleteMapping(value = "/service/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Response destroy(@PathVariable("id") String id) {
        log.info("Stopping managed service with id {}", id);
        DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.fromString(id));
        Deployment deployment = this.orchestratorService.getDestroyHandler(deployTask);
        this.orchestratorService.asyncDestroyService(deployment, deployTask);
        String successMsg = String.format(
                "Task of stop managed service %s start running.", id);
        return Response.successResponse(successMsg);
    }


    /**
     * Get openapi of registered service by id.
     *
     * @param id id of registered service.
     * @return response
     */
    @Tag(name = "Service", description = "APIs to get openapi of service deploy context")
    @GetMapping(value = "/service/openapi/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public String openApi(@PathVariable("id") String id) {
        log.info("Get openapi url of registered service with id {}", id);
        String apiUrl = this.orchestratorService.getOpenApiUrl(id);
        String successMsg = String.format(
                "Get openapi of registered service success with Url %s.", apiUrl);
        log.info(successMsg);
        return apiUrl;
    }

}
