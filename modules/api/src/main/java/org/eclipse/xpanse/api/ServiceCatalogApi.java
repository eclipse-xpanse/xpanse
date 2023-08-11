/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api;

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
import org.eclipse.xpanse.modules.models.servicetemplate.view.CategoryOclVo;
import org.eclipse.xpanse.modules.models.servicetemplate.view.UserAvailableServiceVo;
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
     * Returns the list of all registered services that are available for user to order/deploy.
     *
     * @param categoryName   name of category.
     * @param cspName        name of cloud service provider.
     * @param serviceName    name of registered service.
     * @param serviceVersion version of registered service.
     * @return response
     */
    @Tag(name = "Service Catalog",
            description = "APIs to query the services which are available for the user to order.")
    @Operation(description =
            "Returns the list of all registered services that are available for user to order.")
    @GetMapping(value = "/services/available",
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
     * Returns the list of all registered services in a tree structure
     * with service name
     * representing the root of the tree and service versions
     * representing the branches of the tree.
     * This method is used for providing different views of the available services.
     *
     * @param category name of category.
     * @return response
     */
    @Tag(name = "Service Catalog",
            description = "APIs to query the services which are available for the user to order.")
    @Operation(description = "Get the available services by tree.")
    @GetMapping(value = "/services/available/category/{categoryName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Secured({ROLE_ADMIN, ROLE_ISV, ROLE_USER})
    public List<CategoryOclVo> getAvailableServicesTree(
            @Parameter(name = "categoryName", description = "category of the service")
            @PathVariable(name = "categoryName") Category category) {
        ServiceTemplateQueryModel query = new ServiceTemplateQueryModel();
        query.setCategory(category);
        List<CategoryOclVo> categoryOclList =
                serviceTemplateManage.getManagedServicesTree(query);
        String successMsg = String.format(
                "Get the tree of available services with category %s "
                        + "successful.", category.toValue());
        log.info(successMsg);
        return categoryOclList;
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
    @GetMapping(value = "/services/available/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UserAvailableServiceVo availableServiceDetails(
            @Parameter(name = "id", description = "The id of available service.")
            @PathVariable("id") String id) {
        UserAvailableServiceVo userAvailableServiceVo = convertToUserAvailableServiceVo(
                serviceTemplateManage.getServiceTemplateDetails(id));
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
    @GetMapping(value = "/services/available/{id}/openapi",
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
            ServiceTemplateEntity serviceEntity) {
        if (Objects.nonNull(serviceEntity)) {
            UserAvailableServiceVo userAvailableServiceVo = new UserAvailableServiceVo();
            BeanUtils.copyProperties(serviceEntity, userAvailableServiceVo);
            userAvailableServiceVo.setIcon(serviceEntity.getOcl().getIcon());
            userAvailableServiceVo.setDescription(serviceEntity.getOcl().getDescription());
            userAvailableServiceVo.setNamespace(serviceEntity.getOcl().getNamespace());
            userAvailableServiceVo.setBilling(serviceEntity.getOcl().getBilling());
            userAvailableServiceVo.setFlavors(serviceEntity.getOcl().getFlavors());
            userAvailableServiceVo.setDeployment(serviceEntity.getOcl().getDeployment());
            userAvailableServiceVo.setVariables(
                    serviceEntity.getOcl().getDeployment().getVariables());
            userAvailableServiceVo.setRegions(
                    serviceEntity.getOcl().getCloudServiceProvider().getRegions());
            userAvailableServiceVo.add(
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ServiceCatalogApi.class)
                                    .openApi(serviceEntity.getId().toString()))
                            .withRel("openApi"));
            return userAvailableServiceVo;
        } else {
            return null;
        }
    }
}
