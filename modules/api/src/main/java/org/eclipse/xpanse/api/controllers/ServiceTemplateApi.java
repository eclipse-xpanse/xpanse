/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;


import static org.eclipse.xpanse.api.config.ServiceTemplateEntityConverter.convertToServiceTemplateDetailVo;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ISV;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.api.config.ServiceTemplateEntityConverter;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateQueryModel;
import org.eclipse.xpanse.modules.database.servicetemplatehistory.ServiceTemplateHistoryEntity;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.modules.models.servicetemplatechange.ServiceTemplateChangeInfo;
import org.eclipse.xpanse.modules.servicetemplate.ServiceTemplateManage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "enable.agent.api.only", havingValue = "false", matchIfMissing = true)
public class ServiceTemplateApi {

    @Resource
    private ServiceTemplateManage serviceTemplateManage;
    @Resource
    private OclLoader oclLoader;

    /**
     * Register new service template using ocl model.
     *
     * @param ocl model of Ocl.
     * @return response
     */
    @Tag(name = "ServiceVendor", description = "APIs to manage service templates.")
    @Operation(description = "Register new service template using ocl model.")
    @PostMapping(value = "/service_templates", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public ServiceTemplateChangeInfo register(@Valid @RequestBody Ocl ocl) {
        ServiceTemplateHistoryEntity serviceTemplateHistory =
                serviceTemplateManage.registerServiceTemplate(ocl);
        return new ServiceTemplateChangeInfo(serviceTemplateHistory.getServiceTemplate().getId(),
                serviceTemplateHistory.getChangeId());
    }

    /**
     * Update service template using an id and ocl model.
     *
     * @param ocl model of Ocl.
     * @return response
     */
    @Tag(name = "ServiceVendor", description = "APIs to manage service templates.")
    @Operation(description = "Update service template using id and ocl model.")
    @PutMapping(value = "/service_templates/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public ServiceTemplateChangeInfo update(
            @Parameter(name = "id", description = "id of service template")
            @PathVariable("id") String id,
            @Parameter(name = "isRemoveServiceTemplateUntilApproved",
                    description = "If true, the old service template is also removed from catalog "
                            + "until the updated one is reviewed and approved.")
            @RequestParam(name = "isRemoveServiceTemplateUntilApproved")
            Boolean isRemoveServiceTemplateUntilApproved,
            @Valid @RequestBody Ocl ocl) {
        ServiceTemplateHistoryEntity updateHistory = serviceTemplateManage.updateServiceTemplate(
                UUID.fromString(id), ocl, isRemoveServiceTemplateUntilApproved);
        return new ServiceTemplateChangeInfo(updateHistory.getServiceTemplate().getId(),
                updateHistory.getChangeId());
    }

    /**
     * Register new service template using URL of Ocl file.
     *
     * @param oclLocation URL of Ocl file.
     * @return response
     */
    @Tag(name = "ServiceVendor", description = "APIs to manage service templates.")
    @Operation(description = "Register new service template using URL of Ocl file.")
    @PostMapping(value = "/service_templates/file", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    @AuditApiRequest(methodName = "getCspFromOclLocation")
    public ServiceTemplateChangeInfo fetch(
            @Parameter(name = "oclLocation", description = "URL of Ocl file")
            @RequestParam(name = "oclLocation") String oclLocation) throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create(oclLocation).toURL());
        ServiceTemplateHistoryEntity serviceTemplateHistory =
                serviceTemplateManage.registerServiceTemplate(ocl);
        return new ServiceTemplateChangeInfo(serviceTemplateHistory.getServiceTemplate().getId(),
                serviceTemplateHistory.getChangeId());
    }


    /**
     * Update service template using id and URL of Ocl file.
     *
     * @param id          id of service template.
     * @param oclLocation URL of new Ocl.
     * @return response
     */
    @Tag(name = "ServiceVendor", description = "APIs to manage service templates.")
    @Operation(description = "Update service template using id and URL of Ocl file.")
    @PutMapping(value = "/service_templates/file/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId")
    public ServiceTemplateChangeInfo fetchUpdate(
            @Parameter(name = "id", description = "id of service template")
            @PathVariable(name = "id") String id,
            @Parameter(name = "isRemoveServiceTemplateUntilApproved",
                    description = "If true, the old service template is also removed from catalog "
                            + "until the updated one is reviewed and approved.")
            @RequestParam(name = "isRemoveServiceTemplateUntilApproved")
            Boolean isRemoveServiceTemplateUntilApproved,
            @Parameter(name = "oclLocation", description = "URL of Ocl file")
            @RequestParam(name = "oclLocation") String oclLocation) throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create(oclLocation).toURL());
        ServiceTemplateHistoryEntity updateHistory = serviceTemplateManage.updateServiceTemplate(
                UUID.fromString(id), ocl, isRemoveServiceTemplateUntilApproved);
        return new ServiceTemplateChangeInfo(updateHistory.getServiceTemplate().getId(),
                updateHistory.getChangeId());
    }

    /**
     * Unregister service template using id.
     *
     * @param id id of service template.
     * @return response
     */
    @Tag(name = "ServiceVendor", description = "APIs to manage service templates.")
    @Operation(description = "Unregister service template using id.")
    @PutMapping("/service_templates/unregister/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId")
    public ServiceTemplateDetailVo unregister(
            @Parameter(name = "id", description = "id of service template")
            @PathVariable("id") String id) {
        ServiceTemplateEntity templateEntity =
                serviceTemplateManage.unregisterServiceTemplate(UUID.fromString(id));
        log.info("Unregister service template with id {} successfully.", id);
        return convertToServiceTemplateDetailVo(templateEntity);
    }

    /**
     * Unregister service template using id.
     *
     * @param id id of service template.
     * @return response
     */
    @Tag(name = "ServiceVendor", description = "APIs to manage service templates.")
    @Operation(description = "Re-register the unregistered service template using id.")
    @PutMapping("/service_templates/re-register/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId")
    public ServiceTemplateDetailVo reRegisterServiceTemplate(
            @Parameter(name = "id", description = "id of service template")
            @PathVariable("id") String id) {
        ServiceTemplateEntity templateEntity =
                serviceTemplateManage.reRegisterServiceTemplate(UUID.fromString(id));
        log.info("Re-register service template with id {} successfully.", id);
        return convertToServiceTemplateDetailVo(templateEntity);
    }


    /**
     * Delete service template using id.
     *
     * @param id id of service template.
     */
    @Tag(name = "ServiceVendor", description = "APIs to manage service templates.")
    @Operation(description = "Delete unregistered service template using id.")
    @DeleteMapping("/service_templates/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId")
    public void deleteServiceTemplate(
            @Parameter(name = "id", description = "id of service template")
            @PathVariable("id") String id) {
        serviceTemplateManage.deleteServiceTemplate(UUID.fromString(id));
        log.info("Unregister service template using id {} successfully.", id);
    }

    /**
     * List service templates with query params.
     *
     * @param category                         category of the service.
     * @param csp                              name of the cloud service provider.
     * @param serviceName                      name of the service.
     * @param serviceVersion                   version of the service.
     * @param serviceHostingType               type of the service hosting.
     * @param serviceTemplateRegistrationState state of the service registration.
     * @return service templates
     */
    @Tag(name = "ServiceVendor", description = "APIs to manage service templates.")
    @Operation(description = "List service templates with query params.")
    @GetMapping(value = "/service_templates", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public List<ServiceTemplateDetailVo> getAllServiceTemplatesByIsv(
            @Parameter(name = "categoryName", description = "category of the service")
            @RequestParam(name = "categoryName", required = false) Category category,
            @Parameter(name = "cspName", description = "name of the cloud service provider")
            @RequestParam(name = "cspName", required = false) Csp csp,
            @Parameter(name = "serviceName", description = "name of the service")
            @RequestParam(name = "serviceName", required = false) String serviceName,
            @Parameter(name = "serviceVersion", description = "version of the service")
            @RequestParam(name = "serviceVersion", required = false) String serviceVersion,
            @Parameter(name = "serviceHostingType", description = "who hosts ths cloud resources")
            @RequestParam(name = "serviceHostingType", required = false)
            ServiceHostingType serviceHostingType,
            @Parameter(name = "serviceTemplateRegistrationState",
                    description = "state of service template registration")
            @RequestParam(name = "serviceTemplateRegistrationState", required = false)
            ServiceTemplateRegistrationState serviceTemplateRegistrationState,
            @Parameter(name = "availableInCatalog", description = "is available in catalog")
            @RequestParam(name = "availableInCatalog", required = false)
            Boolean availableInCatalog,
            @Parameter(name = "isUpdatePending", description = "is service template updating")
            @RequestParam(name = "isUpdatePending", required = false)
            Boolean isUpdatePending) {
        ServiceTemplateQueryModel queryRequest = ServiceTemplateQueryModel.builder()
                .category(category).csp(csp).serviceName(serviceName)
                .serviceVersion(serviceVersion).serviceHostingType(serviceHostingType)
                .serviceTemplateRegistrationState(serviceTemplateRegistrationState)
                .availableInCatalog(availableInCatalog)
                .isUpdatePending(isUpdatePending).checkNamespace(true).build();
        List<ServiceTemplateEntity> serviceTemplateEntities =
                serviceTemplateManage.listServiceTemplates(queryRequest);
        log.info(serviceTemplateEntities.size() + " service templates found.");
        return serviceTemplateEntities.stream().sorted(Comparator.comparingInt(
                        serviceTemplateDetailVo -> serviceTemplateDetailVo != null
                                ? serviceTemplateDetailVo.getCsp().ordinal() : -1))
                .map(ServiceTemplateEntityConverter::convertToServiceTemplateDetailVo)
                .toList();
    }

    /**
     * Get details of service template using id.
     *
     * @param id id of service template.
     * @return response
     */
    @Tag(name = "ServiceVendor", description = "APIs to manage service templates.")
    @Operation(description = "Get service template using id.")
    @GetMapping(value = "/service_templates/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId")
    public ServiceTemplateDetailVo details(
            @Parameter(name = "id", description = "id of service template")
            @PathVariable("id") String id) {
        ServiceTemplateEntity templateEntity =
                serviceTemplateManage.getServiceTemplateDetails(UUID.fromString(id), true, false);
        return convertToServiceTemplateDetailVo(templateEntity);
    }
}
