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
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.models.admin.SystemStatus;
import org.eclipse.xpanse.modules.models.admin.enums.HealthStatus;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.eclipse.xpanse.modules.models.service.register.query.RegisteredServiceQuery;
import org.eclipse.xpanse.modules.models.service.view.RegisteredServiceVo;
import org.eclipse.xpanse.modules.register.register.RegisterService;
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
public class ServiceRegisterApi {

    @Resource
    private RegisterService registerService;

    /**
     * Register new service using ocl model.
     *
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage register services.")
    @Operation(description = "Get category list.")
    @GetMapping(value = "/services/categories",
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
    @PostMapping(value = "/services/register",
            consumes = {"application/x-yaml", "application/yml", "application/yaml"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public RegisteredServiceVo register(@Valid @RequestBody Ocl ocl) {
        RegisteredServiceVo registeredServiceVo = convertToRegisteredServiceVo(
                registerService.registerService(ocl));
        log.info("Registering new service successful.");
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
    @PutMapping(value = "/services/register/{id}",
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
                "Update registered service with id %s successful.", id);
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
    @PostMapping(value = "/services/register/file",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public RegisteredServiceVo fetch(
            @Parameter(name = "oclLocation", description = "URL of Ocl file")
            @RequestParam(name = "oclLocation") String oclLocation)
            throws Exception {
        RegisteredServiceVo registeredServiceVo =
                convertToRegisteredServiceVo(registerService.registerServiceByUrl(oclLocation));
        log.info("Register new service by file successful.");
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
    @PutMapping(value = "/services/register/file/{id}",
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
    @DeleteMapping("/services/register/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response unregister(
            @Parameter(name = "id", description = "id of registered service")
            @PathVariable("id") String id) {
        log.info("Unregister registered service using id {}", id);
        registerService.unregisterService(id);
        String successMsg = String.format(
                "Unregister registered service using id %s successful.", id);
        log.info(successMsg);
        return Response.successResponse(Collections.singletonList(successMsg));
    }


    /**
     * List registered service with query params.
     *
     * @param category       name of category.
     * @param cspName        name of cloud service provider.
     * @param serviceName    name of registered service.
     * @param serviceVersion version of registered service.
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage register services.")
    @Operation(description = "List registered service with query params.")
    @GetMapping(value = "/services/register", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<RegisteredServiceVo> listRegisteredServices(
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
        String successMsg = String.format("Listing registered service with query model %s "
                + "successful.", query);
        List<RegisteredServiceVo> registeredServiceVos =
                serviceEntities.stream().map(this::convertToRegisteredServiceVo)
                        .collect(Collectors.toList());
        log.info(successMsg);
        return registeredServiceVos;
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
    @GetMapping(value = "/services/register/{id}",
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

    private RegisteredServiceVo convertToRegisteredServiceVo(RegisterServiceEntity serviceEntity) {
        if (Objects.nonNull(serviceEntity)) {
            RegisteredServiceVo registeredServiceVo = new RegisteredServiceVo();
            BeanUtils.copyProperties(serviceEntity, registeredServiceVo);
            registeredServiceVo.add(
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ServiceDeployerApi.class)
                            .openApi(serviceEntity.getId().toString())).withRel("openApi"));
            return registeredServiceVo;
        }
        return null;
    }

}
