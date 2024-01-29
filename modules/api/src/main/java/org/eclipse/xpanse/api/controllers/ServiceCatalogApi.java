/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ISV;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.FlavorBasic;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.view.UserOrderableServiceVo;
import org.eclipse.xpanse.modules.servicetemplate.ServiceTemplateManage;
import org.springframework.beans.BeanUtils;
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
            "APIs to query the services which are available for the user to order.")
    @Operation(description =
            "List of all registered services which are available for user to order.")
    @GetMapping(value = "/catalog/services",
            produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    @ResponseStatus(HttpStatus.OK)
    public List<UserOrderableServiceVo> listOrderableServices(
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
        List<ServiceTemplateEntity> serviceEntities =
                serviceTemplateManage.listServiceTemplates(
                        categoryName, cspName, serviceName, serviceVersion, serviceHostingType);
        List<UserOrderableServiceVo> userOrderableServiceVos =
                serviceEntities.stream().map(this::convertToUserOrderableServiceVo).sorted(
                                Comparator.comparingInt(o -> {
                                    assert o != null;
                                    return o.getCsp().ordinal();
                                }))
                        .collect(Collectors.toList());
        log.info(serviceEntities.size() + " orderable services found.");
        return userOrderableServiceVos;
    }

    /**
     * Get deployable service by id.
     *
     * @param id The id of deployable service.
     * @return userorderableServiceVoVo
     */
    @Tag(name = "Service Catalog",
            description = "APIs to query the services which are available for the user to order.")
    @Operation(description = "Get deployable service by id.")
    @GetMapping(value = "/catalog/services/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UserOrderableServiceVo getOrderableServiceDetails(
            @Parameter(name = "id", description = "The id of orderable service.")
            @PathVariable("id") String id) {
        UserOrderableServiceVo userOrderableServiceVo = convertToUserOrderableServiceVo(
                serviceTemplateManage.getServiceTemplateDetails(id, false));
        String successMsg = String.format(
                "Get orderable service with id %s successful.", id);
        log.info(successMsg);
        return userOrderableServiceVo;
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
    @Operation(description = "Get the API document of the orderable service.")
    @Secured({ROLE_ADMIN, ROLE_ISV, ROLE_USER})
    public Link openApi(@PathVariable("id") String id) {
        String apiUrl = this.serviceTemplateManage.getOpenApiUrl(id);
        String successMsg = String.format(
                "Get API document of the orderable service successful with Url %s.", apiUrl);
        log.info(successMsg);
        return Link.of(apiUrl, "OpenApi");
    }

    private UserOrderableServiceVo convertToUserOrderableServiceVo(
            ServiceTemplateEntity serviceTemplateEntity) {
        if (Objects.nonNull(serviceTemplateEntity)) {
            UserOrderableServiceVo userOrderableServiceVo = new UserOrderableServiceVo();
            BeanUtils.copyProperties(serviceTemplateEntity, userOrderableServiceVo);
            userOrderableServiceVo.setIcon(serviceTemplateEntity.getOcl().getIcon());
            userOrderableServiceVo.setDescription(
                    serviceTemplateEntity.getOcl().getDescription());
            userOrderableServiceVo.setBilling(serviceTemplateEntity.getOcl().getBilling());
            List<FlavorBasic> flavorBasics = serviceTemplateEntity.getOcl().getFlavors()
                    .stream().map(flavor -> {
                        FlavorBasic flavorBasic = new FlavorBasic();
                        BeanUtils.copyProperties(flavor, flavorBasic);
                        return flavorBasic;
                    }).toList();
            userOrderableServiceVo.setFlavors(flavorBasics);
            userOrderableServiceVo.setVariables(
                    serviceTemplateEntity.getOcl().getDeployment().getVariables());
            userOrderableServiceVo.setRegions(
                    serviceTemplateEntity.getOcl().getCloudServiceProvider().getRegions());
            userOrderableServiceVo.add(
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ServiceCatalogApi.class)
                            .openApi(serviceTemplateEntity.getId().toString())).withRel("openApi"));
            userOrderableServiceVo.setServiceHostingType(
                    serviceTemplateEntity.getOcl().getServiceHostingType());
            userOrderableServiceVo.setServiceProviderContactDetails(
                    serviceTemplateEntity.getOcl().getServiceProviderContactDetails());
            return userOrderableServiceVo;
        }
        return null;
    }
}
