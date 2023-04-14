/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
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
import org.eclipse.xpanse.modules.models.service.CreateRequest;
import org.eclipse.xpanse.modules.models.view.CategoryOclVo;
import org.eclipse.xpanse.modules.models.view.RegisteredServiceVo;
import org.eclipse.xpanse.modules.models.view.ServiceDetailVo;
import org.eclipse.xpanse.modules.models.view.ServiceVo;
import org.eclipse.xpanse.modules.models.view.UserAvailableServiceVo;
import org.eclipse.xpanse.orchestrator.OrchestratorService;
import org.eclipse.xpanse.orchestrator.register.RegisterService;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
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

    @Resource
    private RegisterService registerService;
    @Resource
    private OrchestratorService orchestratorService;

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
    public RegisteredServiceVo register(@Valid @RequestBody Ocl ocl) {
        RegisteredServiceVo registeredServiceVo = convertToRegisteredServiceVo(
                registerService.registerService(ocl));
        log.info("Register new service success.");
        return registeredServiceVo;
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
    public RegisteredServiceVo update(
            @Parameter(name = "id", description = "id of registered service")
            @PathVariable("id") String id, @Valid @RequestBody Ocl ocl) {
        RegisteredServiceVo registeredServiceVo = convertToRegisteredServiceVo(
                registerService.updateRegisteredService(id, ocl));
        String successMsg = String.format(
                "Update registered service with id %s success.", id);
        log.info(successMsg);
        return registeredServiceVo;
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
    public RegisteredServiceVo fetch(
            @Parameter(name = "oclLocation", description = "URL of Ocl file")
            @RequestParam(name = "oclLocation") String oclLocation)
            throws Exception {
        RegisteredServiceVo registeredServiceVo =
                convertToRegisteredServiceVo(registerService.registerServiceByUrl(oclLocation));
        log.info("Register new service by file success.");
        return registeredServiceVo;
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
    public RegisteredServiceVo fetchUpdate(
            @Parameter(name = "id", description = "id of registered service")
            @PathVariable(name = "id") String id,
            @Parameter(name = "oclLocation", description = "URL of Ocl file")
            @RequestParam(name = "oclLocation") String oclLocation)
            throws Exception {
        log.info("Update registered service {} with Url {}", id, oclLocation);
        RegisteredServiceVo registeredServiceVo = convertToRegisteredServiceVo(
                registerService.updateRegisteredServiceByUrl(id,
                        oclLocation));
        String successMsg = String.format(
                "Update registered service %s with Url %s", id, oclLocation);
        log.info(successMsg);
        return registeredServiceVo;
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
        return Response.successResponse(Collections.singletonList(successMsg));
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
    @GetMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<RegisteredServiceVo> listRegisteredServices(
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
            query.setCsp(Csp.getCspByValue(cspName));
        }
        if (StringUtils.isNotBlank(categoryName)) {
            query.setCategory(Category.getCategoryByCatalog(categoryName));
        }
        query.setServiceName(serviceName);
        query.setServiceVersion(serviceVersion);
        log.info("List registered service with query model {}", query);
        List<RegisterServiceEntity> serviceEntities =
                registerService.queryRegisteredServices(query);
        String successMsg = String.format("List registered service with query model %s "
                + "success.", query);
        List<RegisteredServiceVo> registeredServiceVos =
                serviceEntities.stream().map(this::convertToRegisteredServiceVo)
                        .collect(Collectors.toList());
        log.info(successMsg);
        return registeredServiceVos;
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
        Category category = Category.getCategoryByCatalog(categoryName);
        RegisteredServiceQuery query = new RegisteredServiceQuery();
        query.setCategory(category);
        log.info("List registered service with query model {}", query);
        List<CategoryOclVo> categoryOclList =
                registerService.getManagedServicesTree(query);
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
    public RegisteredServiceVo detail(
            @Parameter(name = "id", description = "id of registered service")
            @PathVariable("id") String id) {
        log.info("Get detail of registered service with name {}.", id);
        RegisteredServiceVo registeredServiceVo = convertToRegisteredServiceVo(
                registerService.getRegisteredService(id));
        String successMsg = String.format(
                "Get detail of registered service with name %s success.", id);
        log.info(successMsg);
        return registeredServiceVo;
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
     * Start a task to deploy registered service.
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
                deployRequest.getServiceName(),
                deployRequest.getVersion(), deployRequest.getCsp());
        UUID id = UUID.randomUUID();
        if (StringUtils.isBlank(deployRequest.getCustomerServiceName())) {
            deployRequest.setCustomerServiceName(generateCustomerServiceName(deployRequest));
        }
        DeployTask deployTask = new DeployTask();
        deployRequest.setId(id);
        deployTask.setId(id);
        deployTask.setCreateRequest(deployRequest);
        Deployment deployment = this.orchestratorService.getDeployHandler(deployTask);
        this.orchestratorService.asyncDeployService(deployment, deployTask);
        String successMsg = String.format(
                "Task of start managed service %s-%s-%s start running. UUID %s",
                deployRequest.getServiceName(),
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
        return Response.successResponse(Collections.singletonList(successMsg));
    }


    /**
     * Get openapi of registered service by id.
     *
     * @param id id of registered service.
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage register services.")
    @GetMapping(value = "/register/openapi/{id}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "API to get openapi of service deploy context")
    public Object openApi(@PathVariable("id") String id) {
        log.info("Get openapi url of registered service with id {}", id);
        String apiUrl = this.registerService.getOpenApiUrl(id);
        String successMsg = String.format(
                "Get openapi of registered service success with Url %s.", apiUrl);
        log.info(successMsg);
        return apiUrl;
    }

    /**
     * List the available services.
     *
     * @param categoryName   name of category.
     * @param cspName        name of cloud service provider.
     * @param serviceName    name of registered service.
     * @param serviceVersion version of registered service.
     * @return response
     */
    @Tag(name = "Services Available",
            description = "APIs to query the available services.")
    @Operation(description = "List the available services.")
    @GetMapping(value = "/services/available",
            produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    @ResponseStatus(HttpStatus.OK)
    public List<UserAvailableServiceVo> listAvailableServices(
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
            query.setCsp(Csp.getCspByValue(cspName));
        }
        if (StringUtils.isNotBlank(categoryName)) {
            query.setCategory(Category.getCategoryByCatalog(categoryName));
        }
        query.setServiceName(serviceName);
        query.setServiceVersion(serviceVersion);
        List<RegisterServiceEntity> serviceEntities =
                registerService.queryRegisteredServices(query);
        String successMsg = String.format("List available services with query model %s "
                + "success.", query);
        List<UserAvailableServiceVo> userAvailableServiceVos =
                serviceEntities.stream().map(this::convertToUserAvailableServiceVo)
                        .collect(Collectors.toList());
        log.info(successMsg);
        return userAvailableServiceVos;
    }


    /**
     * Get the available services by tree.
     *
     * @param categoryName name of category.
     * @return response
     */
    @Tag(name = "Services Available",
            description = "APIs to query available services.")
    @Operation(description = "Get the available services by tree.")
    @GetMapping(value = "/services/available/category/{categoryName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryOclVo> getAvailableServicesTree(
            @Parameter(name = "categoryName", description = "category of the service")
            @PathVariable(name = "categoryName", required = false) String categoryName) {
        Category category = Category.getCategoryByCatalog(categoryName);
        RegisteredServiceQuery query = new RegisteredServiceQuery();
        query.setCategory(category);
        List<CategoryOclVo> categoryOclList =
                registerService.getManagedServicesTree(query);
        String successMsg = String.format(
                "Get the tree of available services with category %s "
                        + "success.", categoryName);
        log.info(successMsg);
        return categoryOclList;
    }


    /**
     * Get available service by id.
     *
     * @param id The id of available service.
     * @return userAvailableServiceVo
     */
    @Tag(name = "Services Available",
            description = "APIs to query available services.")
    @Operation(description = "Get available service by id.")
    @GetMapping(value = "/services/available/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UserAvailableServiceVo availableServiceDetail(
            @Parameter(name = "id", description = "The id of available service.")
            @PathVariable("id") String id) {
        UserAvailableServiceVo userAvailableServiceVo = convertToUserAvailableServiceVo(
                registerService.getRegisteredService(id));
        String successMsg = String.format(
                "Get available service with id %s success.", id);
        log.info(successMsg);
        return userAvailableServiceVo;
    }

    /**
     * Get the API document of the available service.
     *
     * @param id The id of available service.
     */
    @Tag(name = "Services Available",
            description = "APIs to query available services.")
    @GetMapping(value = "/services/available/openapi/{id}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Get the API document of the available service.")
    public Object availableServiceOpenApi(@PathVariable("id") String id) {
        String apiUrl = this.registerService.getOpenApiUrl(id);
        String successMsg = String.format(
                "Get API document of the available service success with Url %s.", apiUrl);
        log.info(successMsg);
        return apiUrl;
    }


    private UserAvailableServiceVo convertToUserAvailableServiceVo(
            RegisterServiceEntity serviceEntity) {
        if (Objects.isNull(serviceEntity)) {
            return null;
        }
        UserAvailableServiceVo userAvailableServiceVo = new UserAvailableServiceVo();
        BeanUtils.copyProperties(serviceEntity, userAvailableServiceVo);
        userAvailableServiceVo.setIcon(serviceEntity.getOcl().getIcon());
        userAvailableServiceVo.setDescription(serviceEntity.getOcl().getDescription());
        userAvailableServiceVo.setNamespace(serviceEntity.getOcl().getNamespace());
        userAvailableServiceVo.setBilling(serviceEntity.getOcl().getBilling());
        userAvailableServiceVo.setFlavors(serviceEntity.getOcl().getFlavors());
        userAvailableServiceVo.setVariables(serviceEntity.getOcl().getDeployment().getVariables());
        userAvailableServiceVo.setRegions(
                serviceEntity.getOcl().getCloudServiceProvider().getRegions());
        userAvailableServiceVo.add(
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(OrchestratorApi.class)
                                .availableServiceOpenApi(serviceEntity.getId().toString()))
                        .withRel("openApi"));
        return userAvailableServiceVo;
    }

    private String generateCustomerServiceName(CreateRequest createRequest) {
        if (createRequest.getServiceName().length() > 5) {
            return createRequest.getServiceName().substring(0, 4) + "-"
                    + RandomStringUtils.randomAlphanumeric(5);
        } else {
            return createRequest.getServiceName() + "-"
                    + RandomStringUtils.randomAlphanumeric(5);
        }
    }

    private RegisteredServiceVo convertToRegisteredServiceVo(RegisterServiceEntity serviceEntity) {
        if (Objects.isNull(serviceEntity)) {
            return null;
        }
        RegisteredServiceVo registeredServiceVo = new RegisteredServiceVo();
        BeanUtils.copyProperties(serviceEntity, registeredServiceVo);
        registeredServiceVo.add(
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(OrchestratorApi.class)
                        .openApi(serviceEntity.getId().toString())).withRel("openApi"));
        return registeredServiceVo;
    }

}
