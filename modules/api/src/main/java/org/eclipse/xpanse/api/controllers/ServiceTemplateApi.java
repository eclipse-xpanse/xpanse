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
import org.eclipse.xpanse.modules.database.servicetemplaterequest.ServiceTemplateRequestHistoryEntity;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestHistory;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestInfo;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestStatus;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestType;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
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

/** REST interface methods for managing service templates those can be used to deploy services. */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_ISV})
@ConditionalOnProperty(name = "enable.agent.api.only", havingValue = "false", matchIfMissing = true)
public class ServiceTemplateApi {

    @Resource private ServiceTemplateManage serviceTemplateManage;
    @Resource private OclLoader oclLoader;

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
    public ServiceTemplateRequestInfo register(@Valid @RequestBody Ocl ocl) {
        ServiceTemplateRequestHistoryEntity serviceTemplateHistory =
                serviceTemplateManage.registerServiceTemplate(ocl);
        return new ServiceTemplateRequestInfo(
                serviceTemplateHistory.getServiceTemplate().getId(),
                serviceTemplateHistory.getRequestId(),
                serviceTemplateHistory.getStatus() == ServiceTemplateRequestStatus.IN_REVIEW);
    }

    /**
     * Update service template using an id and ocl model.
     *
     * @param ocl model of Ocl.
     * @return response
     */
    @Tag(name = "ServiceVendor", description = "APIs to manage service templates.")
    @Operation(description = "Update service template using id and ocl model.")
    @PutMapping(
            value = "/service_templates/{serviceTemplateId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId", paramTypes = UUID.class)
    public ServiceTemplateRequestInfo update(
            @Parameter(name = "serviceTemplateId", description = "id of service template")
                    @PathVariable("serviceTemplateId")
                    UUID serviceTemplateId,
            @Parameter(
                            name = "isRemoveServiceTemplateUntilApproved",
                            description =
                                    "If true, the old service template is also removed from catalog"
                                            + " until the updated one is reviewed and approved.")
                    @RequestParam(name = "isRemoveServiceTemplateUntilApproved")
                    Boolean isRemoveServiceTemplateUntilApproved,
            @Valid @RequestBody Ocl ocl) {
        ServiceTemplateRequestHistoryEntity updateHistory =
                serviceTemplateManage.updateServiceTemplate(
                        serviceTemplateId, ocl, isRemoveServiceTemplateUntilApproved);
        return new ServiceTemplateRequestInfo(
                updateHistory.getServiceTemplate().getId(),
                updateHistory.getRequestId(),
                updateHistory.getStatus() == ServiceTemplateRequestStatus.IN_REVIEW);
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
    public ServiceTemplateRequestInfo fetch(
            @Parameter(name = "oclLocation", description = "URL of Ocl file")
                    @RequestParam(name = "oclLocation")
                    String oclLocation)
            throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create(oclLocation).toURL());
        ServiceTemplateRequestHistoryEntity serviceTemplateHistory =
                serviceTemplateManage.registerServiceTemplate(ocl);
        return new ServiceTemplateRequestInfo(
                serviceTemplateHistory.getServiceTemplate().getId(),
                serviceTemplateHistory.getRequestId(),
                serviceTemplateHistory.getStatus() == ServiceTemplateRequestStatus.IN_REVIEW);
    }

    /**
     * Update service template using id and URL of Ocl file.
     *
     * @param serviceTemplateId id of service template.
     * @param oclLocation URL of new Ocl.
     * @return response
     */
    @Tag(name = "ServiceVendor", description = "APIs to manage service templates.")
    @Operation(description = "Update service template using id and URL of Ocl file.")
    @PutMapping(
            value = "/service_templates/file/{serviceTemplateId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId", paramTypes = UUID.class)
    public ServiceTemplateRequestInfo fetchUpdate(
            @Parameter(name = "serviceTemplateId", description = "id of service template")
                    @PathVariable(name = "serviceTemplateId")
                    UUID serviceTemplateId,
            @Parameter(
                            name = "isRemoveServiceTemplateUntilApproved",
                            description =
                                    "If true, the old service template is also removed from catalog"
                                            + " until the updated one is reviewed and approved.")
                    @RequestParam(name = "isRemoveServiceTemplateUntilApproved")
                    Boolean isRemoveServiceTemplateUntilApproved,
            @Parameter(name = "oclLocation", description = "URL of Ocl file")
                    @RequestParam(name = "oclLocation")
                    String oclLocation)
            throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create(oclLocation).toURL());
        ServiceTemplateRequestHistoryEntity updateHistory =
                serviceTemplateManage.updateServiceTemplate(
                        serviceTemplateId, ocl, isRemoveServiceTemplateUntilApproved);
        return new ServiceTemplateRequestInfo(
                updateHistory.getServiceTemplate().getId(),
                updateHistory.getRequestId(),
                updateHistory.getStatus() == ServiceTemplateRequestStatus.IN_REVIEW);
    }

    /**
     * Remove service template from catalog using id.
     *
     * @param serviceTemplateId id of service template.
     * @return response
     */
    @Tag(name = "ServiceVendor", description = "APIs to manage service templates.")
    @Operation(description = "Remove service template from catalog using id.")
    @PutMapping("/service_templates/remove_from_catalog/{serviceTemplateId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId", paramTypes = UUID.class)
    public ServiceTemplateRequestInfo removeFromCatalog(
            @Parameter(name = "serviceTemplateId", description = "id of service template")
                    @PathVariable("serviceTemplateId")
                    UUID serviceTemplateId) {
        ServiceTemplateRequestHistoryEntity unregisterHistory =
                serviceTemplateManage.removeFromCatalog(serviceTemplateId);
        return new ServiceTemplateRequestInfo(
                unregisterHistory.getServiceTemplate().getId(),
                unregisterHistory.getRequestId(),
                unregisterHistory.getStatus() == ServiceTemplateRequestStatus.IN_REVIEW);
    }

    /**
     * Re-register the unregistered service template using id.
     *
     * @param serviceTemplateId id of service template.
     * @return response
     */
    @Tag(name = "ServiceVendor", description = "APIs to manage service templates.")
    @Operation(description = "Re-add the service template to catalog using id.")
    @PutMapping("/service_templates/re_add_to_catalog/{serviceTemplateId}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId", paramTypes = UUID.class)
    public ServiceTemplateRequestInfo reAddToCatalog(
            @Parameter(name = "serviceTemplateId", description = "id of service template")
                    @PathVariable("serviceTemplateId")
                    UUID serviceTemplateId) {
        ServiceTemplateRequestHistoryEntity reRegisterHistory =
                serviceTemplateManage.reAddToCatalog(serviceTemplateId);
        return new ServiceTemplateRequestInfo(
                reRegisterHistory.getServiceTemplate().getId(),
                reRegisterHistory.getRequestId(),
                reRegisterHistory.getStatus() == ServiceTemplateRequestStatus.IN_REVIEW);
    }

    /**
     * Delete service template not in catalog using id.
     *
     * @param serviceTemplateId id of service template.
     */
    @Tag(name = "ServiceVendor", description = "APIs to manage service templates.")
    @Operation(description = "Delete service template not in catalog using id.")
    @DeleteMapping("/service_templates/{serviceTemplateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId", paramTypes = UUID.class)
    public void deleteServiceTemplate(
            @Parameter(name = "serviceTemplateId", description = "id of service template")
                    @PathVariable("serviceTemplateId")
                    UUID serviceTemplateId) {
        serviceTemplateManage.deleteServiceTemplate(serviceTemplateId);
        log.info("Delete service template using id {} successfully.", serviceTemplateId);
    }

    /**
     * List service templates with query params.
     *
     * @param category category of the service.
     * @param csp name of the cloud service provider.
     * @param serviceName name of the service.
     * @param serviceVersion version of the service.
     * @param serviceHostingType type of the service hosting.
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
                    @RequestParam(name = "categoryName", required = false)
                    Category category,
            @Parameter(name = "cspName", description = "name of the cloud service provider")
                    @RequestParam(name = "cspName", required = false)
                    Csp csp,
            @Parameter(name = "serviceName", description = "name of the service")
                    @RequestParam(name = "serviceName", required = false)
                    String serviceName,
            @Parameter(name = "serviceVersion", description = "version of the service")
                    @RequestParam(name = "serviceVersion", required = false)
                    String serviceVersion,
            @Parameter(name = "serviceHostingType", description = "who hosts ths cloud resources")
                    @RequestParam(name = "serviceHostingType", required = false)
                    ServiceHostingType serviceHostingType,
            @Parameter(
                            name = "serviceTemplateRegistrationState",
                            description = "state of service template registration")
                    @RequestParam(name = "serviceTemplateRegistrationState", required = false)
                    ServiceTemplateRegistrationState serviceTemplateRegistrationState,
            @Parameter(name = "isAvailableInCatalog", description = "is available in catalog")
                    @RequestParam(name = "isAvailableInCatalog", required = false)
                    Boolean isAvailableInCatalog,
            @Parameter(
                            name = "isReviewInProgress",
                            description = "is any request in review progress")
                    @RequestParam(name = "isReviewInProgress", required = false)
                    Boolean isReviewInProgress) {
        ServiceTemplateQueryModel queryRequest =
                ServiceTemplateQueryModel.builder()
                        .category(category)
                        .csp(csp)
                        .serviceName(serviceName)
                        .serviceVersion(serviceVersion)
                        .serviceHostingType(serviceHostingType)
                        .serviceTemplateRegistrationState(serviceTemplateRegistrationState)
                        .isAvailableInCatalog(isAvailableInCatalog)
                        .isReviewInProgress(isReviewInProgress)
                        .checkServiceVendor(true)
                        .build();
        List<ServiceTemplateEntity> serviceTemplateEntities =
                serviceTemplateManage.listServiceTemplates(queryRequest);
        log.info(serviceTemplateEntities.size() + " service templates found.");
        return serviceTemplateEntities.stream()
                .sorted(
                        Comparator.comparingInt(
                                serviceTemplateDetailVo ->
                                        serviceTemplateDetailVo != null
                                                ? serviceTemplateDetailVo.getCsp().ordinal()
                                                : -1))
                .map(ServiceTemplateEntityConverter::convertToServiceTemplateDetailVo)
                .toList();
    }

    /**
     * Get details of service template using id.
     *
     * @param serviceTemplateId id of service template.
     * @return response
     */
    @Tag(name = "ServiceVendor", description = "APIs to manage service templates.")
    @Operation(description = "Get service template using id.")
    @GetMapping(
            value = "/service_templates/{serviceTemplateId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId", paramTypes = UUID.class)
    public ServiceTemplateDetailVo getServiceTemplateDetailsById(
            @Parameter(name = "serviceTemplateId", description = "id of service template")
                    @PathVariable("serviceTemplateId")
                    UUID serviceTemplateId) {
        ServiceTemplateEntity templateEntity =
                serviceTemplateManage.getServiceTemplateDetails(serviceTemplateId, true, false);
        return convertToServiceTemplateDetailVo(templateEntity);
    }

    /**
     * List service template history using id of service template.
     *
     * @param serviceTemplateId id of service template.
     * @param requestType type of service template request.
     * @param requestStatus status of service template request.
     * @return list of service template history.
     */
    @Tag(name = "ServiceVendor", description = "APIs to manage service templates.")
    @Operation(
            description =
                    "Get service template requests using id of service template. The returned"
                            + " requests is sorted by the ascending order of the requested"
                            + " time.")
    @GetMapping(
            value = "/service_templates/{serviceTemplateId}/requests",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId", paramTypes = UUID.class)
    public List<ServiceTemplateRequestHistory> getServiceTemplateRequestHistoryByServiceTemplateId(
            @Parameter(name = "serviceTemplateId", description = "id of service template")
                    @PathVariable("serviceTemplateId")
                    UUID serviceTemplateId,
            @Parameter(name = "requestType", description = "type of service template request")
                    @RequestParam(name = "requestType", required = false)
                    ServiceTemplateRequestType requestType,
            @Parameter(name = "requestStatus", description = "status of service template request")
                    @RequestParam(name = "requestStatus", required = false)
                    ServiceTemplateRequestStatus requestStatus) {
        return serviceTemplateManage.getServiceTemplateRequestHistoryByServiceTemplateId(
                serviceTemplateId, requestType, requestStatus);
    }

    /**
     * Get ocl of service template request using change id.
     *
     * @param requestId id of service template request.
     * @return ocl data of service template request.
     */
    @Tag(name = "ServiceVendor", description = "APIs to manage service templates.")
    @Operation(description = "Get requested service template request using request id.")
    @GetMapping(
            value = "/service_templates/requests/{requestId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceTemplateRequestId")
    public Ocl getRequestedServiceTemplateByRequestId(
            @Parameter(name = "requestId", description = "id of service template request")
                    @PathVariable("requestId")
                    UUID requestId) {
        return serviceTemplateManage.getRequestedOclByRequestId(requestId);
    }
}
