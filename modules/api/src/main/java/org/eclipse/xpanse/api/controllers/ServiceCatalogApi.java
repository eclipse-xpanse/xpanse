/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.api.config.ServiceTemplateEntityConverter.convertToUserOrderableServiceVo;
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
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.api.config.ServiceTemplateEntityConverter;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateQueryModel;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateDisabledException;
import org.eclipse.xpanse.modules.models.servicetemplate.view.UserOrderableServiceVo;
import org.eclipse.xpanse.modules.servicetemplate.ServiceTemplateManage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.Link;
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

/** REST interface methods for service catalog. */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
@ConditionalOnProperty(name = "enable.agent.api.only", havingValue = "false", matchIfMissing = true)
public class ServiceCatalogApi {

    @Resource private ServiceTemplateManage serviceTemplateManage;

    /**
     * List all approved service templates which are available for user to order/deploy.
     *
     * @param categoryName category of the service.
     * @param cspName name of the cloud service provider.
     * @param serviceName name of the service.
     * @param serviceVersion version of the service.
     * @param serviceHostingType type of the service hosting.
     * @return service templates
     */
    @Tag(
            name = "ServiceCatalog",
            description = "APIs to query the services which are available for the user to order.")
    @Operation(description = "List of all approved services which are available for user to order.")
    @GetMapping(
            value = "/catalog/services",
            produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public List<UserOrderableServiceVo> getOrderableServices(
            @Parameter(name = "categoryName", description = "category of the service")
                    @RequestParam(name = "categoryName", required = false)
                    Category categoryName,
            @Parameter(name = "cspName", description = "name of the cloud service provider")
                    @RequestParam(name = "cspName", required = false)
                    Csp cspName,
            @Parameter(name = "serviceName", description = "name of the service")
                    @RequestParam(name = "serviceName", required = false)
                    String serviceName,
            @Parameter(name = "serviceVersion", description = "version of the service")
                    @RequestParam(name = "serviceVersion", required = false)
                    String serviceVersion,
            @Parameter(name = "serviceHostingType", description = "who hosts ths cloud resources")
                    @RequestParam(name = "serviceHostingType", required = false)
                    ServiceHostingType serviceHostingType) {

        ServiceTemplateQueryModel queryRequest =
                ServiceTemplateQueryModel.builder()
                        .category(categoryName)
                        .csp(cspName)
                        .serviceName(serviceName)
                        .serviceVersion(serviceVersion)
                        .serviceHostingType(serviceHostingType)
                        .isAvailableInCatalog(true)
                        .checkServiceVendor(false)
                        .build();
        List<ServiceTemplateEntity> serviceTemplateEntities =
                serviceTemplateManage.listServiceTemplates(queryRequest);
        log.info(serviceTemplateEntities.size() + " orderable services found.");
        return serviceTemplateEntities.stream()
                .sorted(Comparator.comparingInt(template -> template.getCsp().ordinal()))
                .map(ServiceTemplateEntityConverter::convertToUserOrderableServiceVo)
                .toList();
    }

    /**
     * Get deployable service by id.
     *
     * @param serviceTemplateId The id of deployable service.
     * @return userOrderableServiceVo
     */
    @Tag(
            name = "ServiceCatalog",
            description = "APIs to query the services which are available for the user to order.")
    @Operation(description = "Get deployable service by id.")
    @GetMapping(
            value = "/catalog/services/{serviceTemplateId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId", paramTypes = UUID.class)
    public UserOrderableServiceVo getOrderableServiceDetailsById(
            @Parameter(name = "serviceTemplateId", description = "The id of orderable service.")
                    @PathVariable("serviceTemplateId")
                    UUID serviceTemplateId) {
        ServiceTemplateEntity serviceTemplateEntity =
                serviceTemplateManage.getServiceTemplateDetails(serviceTemplateId, false, false);
        if (Objects.equals(false, serviceTemplateEntity.getIsAvailableInCatalog())) {
            String errMsg =
                    "Service template with id "
                            + serviceTemplateId
                            + " is disabled to order service.";
            log.error(errMsg);
            throw new ServiceTemplateDisabledException(errMsg);
        }
        String successMsg =
                String.format("Get orderable service with id %s successful.", serviceTemplateId);
        log.info(successMsg);
        return convertToUserOrderableServiceVo(serviceTemplateEntity);
    }

    /**
     * Get the API document of the deployable service.
     *
     * @param serviceTemplateId The id of deployable service.
     */
    @Tag(
            name = "ServiceCatalog",
            description = "APIs to query the services which are available for the user to order.")
    @GetMapping(
            value = "/catalog/services/{serviceTemplateId}/openapi",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Get the API document of the orderable service.")
    @Secured({ROLE_ADMIN, ROLE_ISV, ROLE_USER})
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId", paramTypes = UUID.class)
    public Link openApi(@PathVariable("serviceTemplateId") UUID serviceTemplateId) {
        String apiUrl = this.serviceTemplateManage.getOpenApiUrl(serviceTemplateId);
        String successMsg =
                String.format(
                        "Get API document of the orderable service successful with Url %s.",
                        apiUrl);
        log.info(successMsg);
        return Link.of(apiUrl, "OpenApi");
    }
}
