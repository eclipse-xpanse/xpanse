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
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.api.config.ServiceTemplateEntityConverter;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateQueryModel;
import org.eclipse.xpanse.modules.database.servicetemplaterequest.ServiceTemplateRequestHistoryQueryModel;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.ReviewServiceTemplateRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestToReview;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestStatus;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.eclipse.xpanse.modules.servicetemplate.ServiceTemplateManage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

/** REST interface methods for role csp to manage service templates. */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_CSP})
@ConditionalOnProperty(name = "enable.agent.api.only", havingValue = "false", matchIfMissing = true)
public class CspServiceTemplateApi {

    @Resource private ServiceTemplateManage serviceTemplateManage;
    @Resource private UserServiceHelper userServiceHelper;

    /**
     * List service templates with query params.
     *
     * @param categoryName category of the service.
     * @param serviceName name of the service.
     * @param serviceVersion version of the service.
     * @param serviceHostingType type of the service hosting.
     * @param serviceTemplateRegistrationState state of the service template registration.
     * @return service templates
     */
    @Tag(
            name = "CloudServiceProvider",
            description = "APIs for cloud service provider to manage service templates.")
    @Operation(description = "List managed service templates with query params.")
    @GetMapping(value = "/csp/service_templates", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(enabled = false)
    public List<ServiceTemplateDetailVo> listManagedServiceTemplates(
            @Parameter(name = "categoryName", description = "category of the service")
                    @RequestParam(name = "categoryName", required = false)
                    Category categoryName,
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
        Csp csp = userServiceHelper.getCurrentUserManageCsp();
        ServiceTemplateQueryModel queryRequest =
                ServiceTemplateQueryModel.builder()
                        .csp(csp)
                        .category(categoryName)
                        .serviceName(serviceName)
                        .serviceVersion(serviceVersion)
                        .serviceHostingType(serviceHostingType)
                        .serviceTemplateRegistrationState(serviceTemplateRegistrationState)
                        .isAvailableInCatalog(isAvailableInCatalog)
                        .isReviewInProgress(isReviewInProgress)
                        .checkServiceVendor(false)
                        .build();
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
     * @param serviceTemplateId service template id.
     * @return service template details.
     */
    @Tag(
            name = "CloudServiceProvider",
            description = "APIs for cloud service provider to manage service templates.")
    @Operation(description = "view service template by id.")
    @GetMapping(
            value = "/csp/service_templates/{serviceTemplateId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceTemplateId", paramTypes = UUID.class)
    public ServiceTemplateDetailVo getServiceTemplateDetails(
            @Parameter(name = "serviceTemplateId", description = "id of service template")
                    @PathVariable("serviceTemplateId")
                    UUID serviceTemplateId) {
        ServiceTemplateEntity templateEntity =
                serviceTemplateManage.getServiceTemplateDetails(serviceTemplateId, false, true);
        return convertToServiceTemplateDetailVo(templateEntity);
    }

    /**
     * List pending service template request history to review.
     *
     * @return service templates
     */
    @Tag(
            name = "CloudServiceProvider",
            description = "APIs for cloud service provider to manage service templates.")
    @Operation(description = "Get service template requests pending to review.")
    @GetMapping(
            value = "/csp/service_templates/requests/pending",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(enabled = false)
    public List<ServiceTemplateRequestToReview> getPendingServiceReviewRequests(
            @Parameter(name = "serviceTemplateId", description = "id of service template")
                    @RequestParam(name = "serviceTemplateId", required = false)
                    UUID serviceTemplateId) {
        Csp csp = userServiceHelper.getCurrentUserManageCsp();
        ServiceTemplateRequestHistoryQueryModel queryModel =
                ServiceTemplateRequestHistoryQueryModel.builder()
                        .csp(csp)
                        .serviceTemplateId(serviceTemplateId)
                        .status(ServiceTemplateRequestStatus.IN_REVIEW)
                        .build();
        return serviceTemplateManage.getPendingServiceTemplateRequests(queryModel);
    }

    /**
     * Review service template request.
     *
     * @param requestId service template request id.
     * @param reviewServiceTemplateRequest review request for service template registration.
     */
    @Tag(
            name = "CloudServiceProvider",
            description = "APIs for cloud service provider to manage service templates.")
    @Operation(description = "Submit review result for a service template request.")
    @PutMapping(
            value = "/csp/service_templates/requests/review/{requestId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuditApiRequest(methodName = "getCspFromServiceTemplateRequestId", paramTypes = UUID.class)
    public void reviewServiceTemplateRequest(
            @Parameter(name = "requestId", description = "id of service template request")
                    @PathVariable(name = "requestId")
                    UUID requestId,
            @Valid @RequestBody ReviewServiceTemplateRequest reviewServiceTemplateRequest) {
        serviceTemplateManage.reviewServiceTemplateRequest(requestId, reviewServiceTemplateRequest);
    }
}
