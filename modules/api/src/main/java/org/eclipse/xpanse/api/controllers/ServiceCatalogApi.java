/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_ISV;
import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.query.ServiceTemplateQueryModel;
import org.eclipse.xpanse.modules.models.servicetemplate.view.UserAvailableServiceVo;
import org.eclipse.xpanse.modules.servicetemplate.ServiceTemplateManage;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST interface methods for service catalog.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
public class ServiceCatalogApi {

    @Resource
    private ServiceTemplateManage serviceTemplateManage;

    /**
     * List all registered service templates which are available for user to order/deploy.
     *
     * @param categoryName   name of category.
     * @param cspName        name of cloud service provider.
     * @param serviceName    name of registered service.
     * @param serviceVersion version of registered service.
     * @return response
     */
    @Tag(name = "Service Catalog", description =
            "APIs to query service templates which are available for the user to order.")
    @Operation(description =
            "List of all registered services which are available for user to order.")
    @GetMapping(value = "/catalog/services",
            produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    @ResponseStatus(HttpStatus.OK)
    public List<UserAvailableServiceVo> listAvailableServices(
            @Parameter(name = "categoryName", description = "category of the service")
            @RequestParam(name = "categoryName", required = false) Category categoryName,
            @Parameter(name = "cspName", description = "name of the cloud service provider")
            @RequestParam(name = "cspName", required = false) Csp cspName,
            @Parameter(name = "serviceName", description = "name of the service")
            @RequestParam(name = "serviceName", required = false) String serviceName,
            @Parameter(name = "serviceVersion", description = "version of the service")
            @RequestParam(name = "serviceVersion", required = false) String serviceVersion) {
        ServiceTemplateQueryModel query = getServiceTemplatesQueryModel(
                categoryName, cspName, serviceName, serviceVersion);
        List<ServiceTemplateEntity> serviceEntities =
                serviceTemplateManage.listServiceTemplates(query);
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
     * Get deployable service by id.
     *
     * @param id The id of deployable service.
     * @return userAvailableServiceVo
     */
    @Tag(name = "Service Catalog",
            description = "APIs to query the services which are available for the user to order.")
    @Operation(description = "Get deployable service by id.")
    @GetMapping(value = "/catalog/services/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UserAvailableServiceVo availableServiceDetails(
            @Parameter(name = "id", description = "The id of available service.")
            @PathVariable("id") String id) {
        UserAvailableServiceVo userAvailableServiceVo = convertToUserAvailableServiceVo(
                serviceTemplateManage.getServiceTemplateDetails(id, false));
        String successMsg = String.format(
                "Get available service with id %s successful.", id);
        log.info(successMsg);
        return userAvailableServiceVo;
    }

    /**
     * Get the API document of the deployable service.
     *
     * @param id The id of deployable service.
     */
    @Tag(name = "Service Catalog",
            description = "APIs to query the services which are available for the user to order.")
    @GetMapping(value = "/catalog/services/{id}/openapi",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Get the API document of the available service.")
    @Secured({ROLE_ADMIN, ROLE_ISV, ROLE_USER})
    public Link openApi(@PathVariable("id") String id) {
        String apiUrl = this.serviceTemplateManage.getOpenApiUrl(id);
        String successMsg = String.format(
                "Get API document of the available service successful with Url %s.", apiUrl);
        log.info(successMsg);
        return Link.of(apiUrl, "OpenApi");
    }

    private ServiceTemplateQueryModel getServiceTemplatesQueryModel(Category category, Csp csp,
                                                                    String serviceName,
                                                                    String serviceVersion) {
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
        return query;
    }

    private UserAvailableServiceVo convertToUserAvailableServiceVo(
            ServiceTemplateEntity serviceTemplateEntity) {
        UserAvailableServiceVo catalogVo =
                serviceTemplateManage.convertToUserAvailableServiceVo(serviceTemplateEntity);
        if (Objects.nonNull(catalogVo)) {
            catalogVo.add(
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ServiceCatalogApi.class)
                            .openApi(serviceTemplateEntity.getId().toString())).withRel("openApi"));
        }
        return catalogVo;
    }
}
