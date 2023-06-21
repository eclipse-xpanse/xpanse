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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.CreateRequest;
import org.eclipse.xpanse.modules.models.service.register.query.RegisteredServiceQuery;
import org.eclipse.xpanse.modules.models.service.view.CategoryOclVo;
import org.eclipse.xpanse.modules.models.service.view.ServiceDetailVo;
import org.eclipse.xpanse.modules.models.service.view.ServiceVo;
import org.eclipse.xpanse.modules.models.service.view.UserAvailableServiceVo;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployment;
import org.eclipse.xpanse.modules.register.register.RegisterService;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
public class ServiceDeployerApi {

    @Resource
    private RegisterService registerService;
    @Resource
    private DeployService deployService;

    /**
     * Get status of the managed service with name.
     *
     * @return Status of the managed service.
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Get deployed service details by id.")
    @GetMapping(value = "/services/deployed/{id}/details/{userName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ServiceDetailVo getDeployedServiceDetailsById(
            @Parameter(name = "id", description = "Task id of deployed service")
            @PathVariable("id") String id,
            @Parameter(name = "userName", description = "User who deployed the service")
            @PathVariable("userName") String userName) {
        return this.deployService.getDeployServiceDetails(UUID.fromString(id), userName);
    }

    /**
     * List all deployed services.
     *
     * @return list of all services deployed.
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Lists all deployed services.")
    @GetMapping(value = "/services/deployed", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<ServiceVo> listDeployedServices() {
        return this.deployService.getDeployedServices();
    }

    /**
     * List all deployed services by a user.
     *
     * @return list of all services deployed by a user.
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "List all deployed services by a user.")
    @GetMapping(value = "/services/deployed/{userName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<ServiceVo> getDeployedServicesByUser(
            @Parameter(name = "userName", description = "User who deployed the service")
            @PathVariable("userName") String userName) {
        return this.deployService
                .getDeployedServices()
                .stream()
                .filter(serviceVo -> Objects.nonNull(serviceVo.getUserName())
                        && serviceVo.getUserName().equals(userName))
                .collect(Collectors.toList());

    }

    /**
     * Start a task to deploy registered service.
     *
     * @param deployRequest the managed service to create.
     * @return response
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Start a task to deploy registered service.")
    @PostMapping(value = "/services/deploy", produces = MediaType.APPLICATION_JSON_VALUE)
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
        Deployment deployment = this.deployService.getDeployHandler(deployTask);
        this.deployService.asyncDeployService(deployment, deployTask);
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
    @DeleteMapping(value = "/services/destroy/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Response destroy(@PathVariable("id") String id) {
        log.info("Stopping managed service with id {}", id);
        DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.fromString(id));
        Deployment deployment = this.deployService.getDestroyHandler(deployTask);
        this.deployService.asyncDestroyService(deployment, deployTask);
        String successMsg = String.format(
                "Task of stop managed service %s start running.", id);
        return Response.successResponse(Collections.singletonList(successMsg));
    }


    /**
     * List the available services.
     *
     * @param category       name of category.
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
            @RequestParam(name = "categoryName", required = false) Category category,
            @Parameter(name = "cspName", description = "name of the service provider")
            @RequestParam(name = "cspName", required = false) String cspName,
            @Parameter(name = "serviceName", description = "name of the service")
            @RequestParam(name = "serviceName", required = false) String serviceName,
            @Parameter(name = "serviceVersion", description = "version of the service")
            @RequestParam(name = "serviceVersion", required = false) String serviceVersion) {
        RegisteredServiceQuery query = getServicesQueryModel(category, cspName, serviceName,
                serviceVersion);
        List<RegisterServiceEntity> serviceEntities =
                registerService.queryRegisteredServices(query);
        String successMsg = String.format("Listing available services with query model %s "
                + "successful.", query);
        List<UserAvailableServiceVo> userAvailableServiceVos =
                serviceEntities.stream().map(this::convertToUserAvailableServiceVo).sorted(
                                Comparator.comparingInt(o -> {
                                    assert o != null;
                                    return o.getCsp().ordinal();
                                }))
                        .collect(Collectors.toList());
        log.info(successMsg);
        return userAvailableServiceVos;
    }


    /**
     * Get the available services by tree.
     *
     * @param category name of category.
     * @return response
     */
    @Tag(name = "Services Available",
            description = "APIs to query the available services.")
    @Operation(description = "Get the available services by tree.")
    @GetMapping(value = "/services/available/category/{categoryName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryOclVo> getAvailableServicesTree(
            @Parameter(name = "categoryName", description = "category of the service")
            @PathVariable(name = "categoryName", required = false) Category category) {
        RegisteredServiceQuery query = new RegisteredServiceQuery();
        query.setCategory(category);
        List<CategoryOclVo> categoryOclList =
                registerService.getManagedServicesTree(query);
        String successMsg = String.format(
                "Get the tree of available services with category %s "
                        + "successful.", category.toValue());
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
            description = "APIs to query the available services.")
    @Operation(description = "Get available service by id.")
    @GetMapping(value = "/services/available/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UserAvailableServiceVo availableServiceDetails(
            @Parameter(name = "id", description = "The id of available service.")
            @PathVariable("id") String id) {
        UserAvailableServiceVo userAvailableServiceVo = convertToUserAvailableServiceVo(
                registerService.getRegisteredService(id));
        String successMsg = String.format(
                "Get available service with id %s successful.", id);
        log.info(successMsg);
        return userAvailableServiceVo;
    }

    /**
     * Get the API document of the available service.
     *
     * @param id The id of available service.
     */
    @Tag(name = "Services Available",
            description = "APIs to query the available services.")
    @GetMapping(value = "/services/available/{id}/openapi",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Get the API document of the available service.")
    public Link openApi(@PathVariable("id") String id) {
        String apiUrl = this.registerService.getOpenApiUrl(id);
        String successMsg = String.format(
                "Get API document of the available service successful with Url %s.", apiUrl);
        log.info(successMsg);
        return Link.of(apiUrl, "OpenApi");
    }


    private RegisteredServiceQuery getServicesQueryModel(Category category, String cspName,
                                                         String serviceName,
                                                         String serviceVersion) {
        RegisteredServiceQuery query = new RegisteredServiceQuery();
        if (StringUtils.isNotBlank(cspName)) {
            query.setCsp(Csp.getByValue(cspName));
        }
        query.setCategory(category);

        if (StringUtils.isNotBlank(serviceName)) {
            query.setServiceName(serviceName);
        }
        if (StringUtils.isNotBlank(serviceVersion)) {
            query.setServiceVersion(serviceVersion);
        }
        return query;

    }


    private UserAvailableServiceVo convertToUserAvailableServiceVo(
            RegisterServiceEntity serviceEntity) {
        if (Objects.nonNull(serviceEntity)) {
            UserAvailableServiceVo userAvailableServiceVo = new UserAvailableServiceVo();
            BeanUtils.copyProperties(serviceEntity, userAvailableServiceVo);
            userAvailableServiceVo.setIcon(serviceEntity.getOcl().getIcon());
            userAvailableServiceVo.setDescription(serviceEntity.getOcl().getDescription());
            userAvailableServiceVo.setNamespace(serviceEntity.getOcl().getNamespace());
            userAvailableServiceVo.setBilling(serviceEntity.getOcl().getBilling());
            userAvailableServiceVo.setFlavors(serviceEntity.getOcl().getFlavors());
            userAvailableServiceVo.setVariables(
                    serviceEntity.getOcl().getDeployment().getVariables());
            userAvailableServiceVo.setRegions(
                    serviceEntity.getOcl().getCloudServiceProvider().getRegions());
            userAvailableServiceVo.add(
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ServiceDeployerApi.class)
                                    .openApi(serviceEntity.getId().toString()))
                            .withRel("openApi"));
            return userAvailableServiceVo;
        } else {
            return null;
        }
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
}
