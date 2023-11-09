/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;


import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_ISV;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.security.model.CurrentUserInfo;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.query.ServiceTemplateQueryModel;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.eclipse.xpanse.modules.servicetemplate.ServiceTemplateManage;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
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
 * REST interface methods for managing service templates those can be used to deploy services.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_ISV})
public class ServiceTemplateApi {

    @Resource
    private ServiceTemplateManage serviceTemplateManage;

    @Resource
    private IdentityProviderManager identityProviderManager;

    /**
     * Register new service template using ocl model.
     *
     * @param ocl model of Ocl.
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage service templates.")
    @Operation(description = "Register new service template using ocl model.")
    @PostMapping(value = "/service_templates",
            consumes = {"application/x-yaml", "application/yml", "application/yaml"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public ServiceTemplateDetailVo register(@Valid @RequestBody Ocl ocl) {
        ServiceTemplateDetailVo serviceTemplateDetailVo = convertToServiceTemplateDetailVo(
                serviceTemplateManage.registerServiceTemplate(ocl));
        log.info("Registering new service successful.");
        return serviceTemplateDetailVo;
    }

    /**
     * Update service template using an id and ocl model.
     *
     * @param ocl model of Ocl.
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage service templates.")
    @Operation(description = "Update service template using id and ocl model.")
    @PutMapping(value = "/service_templates/{id}",
            consumes = {"application/x-yaml", "application/yml", "application/yaml"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public ServiceTemplateDetailVo update(
            @Parameter(name = "id", description = "id of service template")
            @PathVariable("id") String id, @Valid @RequestBody Ocl ocl) {
        ServiceTemplateDetailVo serviceTemplateDetailVo = convertToServiceTemplateDetailVo(
                serviceTemplateManage.updateServiceTemplate(id, ocl));
        String successMsg = String.format(
                "Update service template with id %s successful.", id);
        log.info(successMsg);
        return serviceTemplateDetailVo;
    }

    /**
     * Register new service template using URL of Ocl file.
     *
     * @param oclLocation URL of Ocl file.
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage service templates.")
    @Operation(description = "Register new service template using URL of Ocl file.")
    @PostMapping(value = "/service_templates/file", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public ServiceTemplateDetailVo fetch(
            @Parameter(name = "oclLocation", description = "URL of Ocl file")
            @RequestParam(name = "oclLocation") String oclLocation)
            throws Exception {
        ServiceTemplateDetailVo serviceTemplateDetailVo =
                convertToServiceTemplateDetailVo(
                        serviceTemplateManage.registerServiceTemplateByUrl(oclLocation));
        String message = String.format("Register new service template by file with URL "
                + "%s successful.", oclLocation);
        log.info(message);
        return serviceTemplateDetailVo;
    }


    /**
     * Update service template using id and URL of Ocl file.
     *
     * @param id          id of service template.
     * @param oclLocation URL of new Ocl.
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage service templates.")
    @Operation(description = "Update service template using id and URL of Ocl file.")
    @PutMapping(value = "/service_templates/file/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public ServiceTemplateDetailVo fetchUpdate(
            @Parameter(name = "id", description = "id of service template")
            @PathVariable(name = "id") String id,
            @Parameter(name = "oclLocation", description = "URL of Ocl file")
            @RequestParam(name = "oclLocation") String oclLocation)
            throws Exception {
        log.info("Update service template {} with Url {}", id, oclLocation);
        ServiceTemplateDetailVo serviceTemplateDetailVo = convertToServiceTemplateDetailVo(
                serviceTemplateManage.updateServiceTemplateByUrl(id,
                        oclLocation));
        String successMsg = String.format(
                "Update service template %s with Url %s", id, oclLocation);
        log.info(successMsg);
        return serviceTemplateDetailVo;
    }

    /**
     * Unregister service template using id.
     *
     * @param id id of service template.
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage service templates.")
    @Operation(description = "Delete service template using id.")
    @DeleteMapping("/service_templates/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public Response unregister(
            @Parameter(name = "id", description = "id of service template")
            @PathVariable("id") String id) {
        serviceTemplateManage.unregisterServiceTemplate(id);
        String successMsg = String.format(
                "Unregister service template using id %s successful.", id);
        log.info(successMsg);
        return Response.successResponse(Collections.singletonList(successMsg));
    }


    /**
     * List service templates with query params.
     *
     * @param categoryName   name of category.
     * @param cspName        name of cloud service provider.
     * @param serviceName    name of service template.
     * @param serviceVersion version of service template.
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage service templates.")
    @Operation(description = "List service templates with query params.")
    @GetMapping(value = "/service_templates", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<ServiceTemplateDetailVo> listServiceTemplates(
            @Parameter(name = "categoryName", description = "category of the service")
            @RequestParam(name = "categoryName", required = false) Category categoryName,
            @Parameter(name = "cspName", description = "name of the cloud service provider")
            @RequestParam(name = "cspName", required = false) Csp cspName,
            @Parameter(name = "serviceName", description = "name of the service")
            @RequestParam(name = "serviceName", required = false) String serviceName,
            @Parameter(name = "serviceVersion", description = "version of the service")
            @RequestParam(name = "serviceVersion", required = false) String serviceVersion,
            @Parameter(name = "serviceHostingType", description = "who hosts ths cloud resources")
            @RequestParam(name = "serviceHostingType", required = false)
            ServiceHostingType serviceHostingType) {
        ServiceTemplateQueryModel query = getServiceTemplateQueryModel(
                categoryName, cspName, serviceName, serviceVersion, serviceHostingType);
        List<ServiceTemplateEntity> serviceEntities =
                serviceTemplateManage.listServiceTemplates(query);
        String successMsg = String.format("Listing service templates with query model %s "
                + "successful.", query);
        List<ServiceTemplateDetailVo> serviceTemplateDetailVos =
                serviceEntities.stream().map(this::convertToServiceTemplateDetailVo).sorted(
                                Comparator.comparingInt(o -> {
                                    assert o != null;
                                    return o.getCsp().ordinal();
                                }))
                        .toList();
        log.info(successMsg);
        return serviceTemplateDetailVos;
    }

    /**
     * Get service template using id.
     *
     * @param id id of service template.
     * @return response
     */
    @Tag(name = "Service Vendor",
            description = "APIs to manage service templates.")
    @Operation(description = "Get service template using id.")
    @GetMapping(value = "/service_templates/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ServiceTemplateDetailVo details(
            @Parameter(name = "id", description = "id of service template")
            @PathVariable("id") String id) {
        ServiceTemplateDetailVo serviceTemplateDetailVo = convertToServiceTemplateDetailVo(
                serviceTemplateManage.getServiceTemplateDetails(id, true));
        String successMsg = String.format(
                "Get detail of service template with id %s success.", id);
        log.info(successMsg);
        return serviceTemplateDetailVo;
    }

    private ServiceTemplateQueryModel getServiceTemplateQueryModel(
            Category category,
            Csp csp,
            String serviceName,
            String serviceVersion,
            ServiceHostingType serviceHostingType) {
        ServiceTemplateQueryModel query = new ServiceTemplateQueryModel();
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
        if (Objects.nonNull(serviceHostingType)) {
            query.setServiceHostingType(serviceHostingType);
        }
        CurrentUserInfo currentUserInfo = identityProviderManager.getCurrentUserInfo();
        if (Objects.nonNull(currentUserInfo)
                && StringUtils.isNotEmpty(currentUserInfo.getNamespace())) {
            query.setNamespace(currentUserInfo.getNamespace());
        }
        return query;
    }

    private ServiceTemplateDetailVo convertToServiceTemplateDetailVo(
            ServiceTemplateEntity serviceTemplateEntity) {
        if (Objects.nonNull(serviceTemplateEntity)) {
            ServiceTemplateDetailVo serviceTemplateDetailVo = new ServiceTemplateDetailVo();
            BeanUtils.copyProperties(serviceTemplateEntity, serviceTemplateDetailVo);
            serviceTemplateDetailVo.setIcon(serviceTemplateEntity.getOcl().getIcon());
            serviceTemplateDetailVo.setDescription(
                    serviceTemplateEntity.getOcl().getDescription());
            serviceTemplateDetailVo.setNamespace(serviceTemplateEntity.getOcl().getNamespace());
            serviceTemplateDetailVo.setBilling(serviceTemplateEntity.getOcl().getBilling());
            serviceTemplateDetailVo.setFlavors(serviceTemplateEntity.getOcl().getFlavors());
            serviceTemplateDetailVo.setDeployment(serviceTemplateEntity.getOcl().getDeployment());
            serviceTemplateDetailVo.setVariables(
                    serviceTemplateEntity.getOcl().getDeployment().getVariables());
            serviceTemplateDetailVo.setRegions(
                    serviceTemplateEntity.getOcl().getCloudServiceProvider().getRegions());
            serviceTemplateDetailVo.add(
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ServiceCatalogApi.class)
                            .openApi(serviceTemplateEntity.getId().toString())).withRel("openApi"));
            serviceTemplateDetailVo.setServiceHostingType(
                    serviceTemplateEntity.getServiceHostingType());
            return serviceTemplateDetailVo;
        }
        return null;
    }
}
