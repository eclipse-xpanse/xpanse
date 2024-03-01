/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;


import static org.eclipse.xpanse.api.config.ServiceTemplateEntityConverter.convertToServiceTemplateDetailVo;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_CSP;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.ServiceTemplateEntityConverter;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateQueryModel;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.ReviewRegistrationRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.eclipse.xpanse.modules.servicetemplate.ServiceTemplateManage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST interface methods for role csp to manage service templates.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_CSP})
public class CspServiceTemplateApi {

    @Resource
    private ServiceTemplateManage serviceTemplateManage;
    @Resource
    private IdentityProviderManager identityProviderManager;

    /**
     * List service templates with query params.
     *
     * @param categoryName             category of the service.
     * @param serviceName              name of the service.
     * @param serviceVersion           version of the service.
     * @param serviceHostingType       type of the service hosting.
     * @param serviceRegistrationState state of the service registration.
     * @return service templates
     */
    @Tag(name = "Cloud Service Provider",
            description = "APIs for cloud service provider to manage service templates.")
    @Operation(description = "List managed service templates with query params.")
    @GetMapping(value = "/csp/service_templates", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<ServiceTemplateDetailVo> listManagedServiceTemplates(
            @Parameter(name = "categoryName", description = "category of the service")
            @RequestParam(name = "categoryName", required = false) Category categoryName,
            @Parameter(name = "serviceName", description = "name of the service")
            @RequestParam(name = "serviceName", required = false) String serviceName,
            @Parameter(name = "serviceVersion", description = "version of the service")
            @RequestParam(name = "serviceVersion", required = false) String serviceVersion,
            @Parameter(name = "serviceHostingType", description = "who hosts ths cloud resources")
            @RequestParam(name = "serviceHostingType", required = false)
            ServiceHostingType serviceHostingType,
            @Parameter(name = "serviceRegistrationState", description = "state of registration")
            @RequestParam(name = "serviceRegistrationState", required = false)
            ServiceRegistrationState serviceRegistrationState) {
        Optional<Csp> cspOptional = identityProviderManager.getCspFromMetadata();
        Csp cspName = cspOptional.orElse(null);
        ServiceTemplateQueryModel queryRequest =
                new ServiceTemplateQueryModel(categoryName, cspName, serviceName, serviceVersion,
                        serviceHostingType, serviceRegistrationState, false);
        List<ServiceTemplateEntity> serviceTemplateEntities =
                serviceTemplateManage.listServiceTemplates(queryRequest);
        log.info(serviceTemplateEntities.size() + " service templates found.");
        return serviceTemplateEntities.stream()
                .map(ServiceTemplateEntityConverter::convertToServiceTemplateDetailVo)
                .toList();
    }


    /**
     * View details of service template registration.
     *
     * @param id id of service template.
     * @return service template details.
     */
    @Tag(name = "Cloud Service Provider",
            description = "APIs for cloud service provider to manage service templates.")
    @Operation(description = "view service template by id.")
    @GetMapping(value = "/csp/service_templates/{id}", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ServiceTemplateDetailVo getRegistrationDetails(
            @Parameter(name = "id", description = "id of service template")
            @PathVariable("id") String id) {
        ServiceTemplateEntity templateEntity =
                serviceTemplateManage.getServiceTemplateDetails(UUID.fromString(id), false, true);
        return convertToServiceTemplateDetailVo(templateEntity);
    }

    /**
     * Review service template registration.
     *
     * @param id                        id of service template.
     * @param reviewRegistrationRequest review request for service template registration.
     */
    @Tag(name = "Cloud Service Provider",
            description = "APIs for cloud service provider to manage service templates.")
    @Operation(description = "Review service template registration.")
    @PutMapping(value = "/service_templates/review/{id}", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reviewRegistration(
            @Parameter(name = "id", description = "id of service template")
            @PathVariable("id") String id,
            @Valid @RequestBody ReviewRegistrationRequest reviewRegistrationRequest) {
        serviceTemplateManage.reviewServiceTemplateRegistration(UUID.fromString(id),
                reviewRegistrationRequest);
    }
}
