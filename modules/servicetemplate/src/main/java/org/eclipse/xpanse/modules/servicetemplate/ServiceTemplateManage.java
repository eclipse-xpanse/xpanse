/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.servicetemplate;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentStorage;
import org.eclipse.xpanse.modules.database.service.ServiceQueryModel;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateQueryModel;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.servicetemplaterequest.ServiceTemplateRequestHistoryEntity;
import org.eclipse.xpanse.modules.database.servicetemplaterequest.ServiceTemplateRequestHistoryQueryModel;
import org.eclipse.xpanse.modules.database.servicetemplaterequest.ServiceTemplateRequestHistoryStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.deployment.DeployerKindManager;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.OpenApiFileGenerationException;
import org.eclipse.xpanse.modules.models.service.utils.ServiceDeployVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ReviewServiceTemplateRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavor;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceReviewResult;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidServiceFlavorsException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidServiceVersionException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.OpenTofuScriptFormatInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.TerraformScriptFormatInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestHistory;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestToReview;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestStatus;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestType;
import org.eclipse.xpanse.modules.models.servicetemplate.request.exceptions.ReviewServiceTemplateRequestNotAllowed;
import org.eclipse.xpanse.modules.models.servicetemplate.request.exceptions.ServiceTemplateRequestNotAllowed;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidateDiagnostics;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.eclipse.xpanse.modules.servicetemplate.price.BillingConfigValidator;
import org.eclipse.xpanse.modules.servicetemplate.utils.AvailabilityZoneSchemaValidator;
import org.eclipse.xpanse.modules.servicetemplate.utils.DeployVariableSchemaValidator;
import org.eclipse.xpanse.modules.servicetemplate.utils.IconProcessorUtil;
import org.eclipse.xpanse.modules.servicetemplate.utils.ServiceActionTemplateValidator;
import org.eclipse.xpanse.modules.servicetemplate.utils.ServiceConfigurationParameterValidator;
import org.eclipse.xpanse.modules.servicetemplate.utils.ServiceTemplateOpenApiGenerator;
import org.semver4j.Semver;
import org.semver4j.SemverException;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/** Implement Interface to manage service template newTemplate in database. */
@Slf4j
@Service
public class ServiceTemplateManage {

    private static final String AUTO_APPROVED_REVIEW_COMMENT = "auto-approved by CSP";
    @Resource private ServiceTemplateStorage templateStorage;
    @Resource private ServiceTemplateRequestHistoryStorage templateRequestStorage;
    @Resource private ServiceDeploymentStorage serviceDeploymentStorage;
    @Resource private ServiceTemplateOpenApiGenerator serviceTemplateOpenApiGenerator;
    @Resource private UserServiceHelper userServiceHelper;

    @Resource
    private ServiceDeployVariablesJsonSchemaGenerator serviceDeployVariablesJsonSchemaGenerator;

    @Resource private DeployerKindManager deployerKindManager;
    @Resource private BillingConfigValidator billingConfigValidator;
    @Resource private PluginManager pluginManager;
    @Resource private ServiceConfigurationParameterValidator serviceConfigurationParameterValidator;
    @Resource private ServiceActionTemplateValidator serviceActionTemplateValidator;

    /**
     * Register service template using the ocl.
     *
     * @param ocl the Ocl model describing the service template.
     * @return Returns service template history entity.
     */
    public ServiceTemplateRequestHistoryEntity registerServiceTemplate(Ocl ocl) {
        ServiceTemplateQueryModel queryModel =
                ServiceTemplateQueryModel.builder()
                        .category(ocl.getCategory())
                        .csp(ocl.getCloudServiceProvider().getName())
                        .serviceName(ocl.getName())
                        .serviceHostingType(ocl.getServiceHostingType())
                        .build();
        List<ServiceTemplateEntity> existingServiceTemplates =
                templateStorage.listServiceTemplates(queryModel);
        validateServiceVersion(ocl.getServiceVersion(), existingServiceTemplates);
        boolean isAutoApprovedEnabled =
                isAutoApproveEnabledForCsp(ocl.getCloudServiceProvider().getName());
        ServiceTemplateEntity serviceTemplateToRegister = createServiceTemplateEntity(ocl);
        updateServiceTemplateRegistrationState(serviceTemplateToRegister, isAutoApprovedEnabled);
        ServiceTemplateEntity registeredServiceTemplate =
                templateStorage.storeAndFlush(serviceTemplateToRegister);
        serviceTemplateOpenApiGenerator.generateServiceApi(registeredServiceTemplate);
        ServiceTemplateRequestType requestType = ServiceTemplateRequestType.REGISTER;
        return createServiceTemplateHistory(
                isAutoApprovedEnabled, requestType, registeredServiceTemplate);
    }

    /**
     * Update service template using id and the ocl model.
     *
     * @param id id of the service template.
     * @param ocl the Ocl model describing the service template.
     * @param isRemoveFromCatalogUntilApproved If remove the service template from catalog until the
     *     updated one is approved.
     * @return Returns service template history entity.
     */
    public ServiceTemplateRequestHistoryEntity updateServiceTemplate(
            UUID id, Ocl ocl, boolean isRemoveFromCatalogUntilApproved) {
        ServiceTemplateEntity serviceTemplateToUpdate = getServiceTemplateDetails(id, true, false);
        ServiceTemplateRegistrationState registrationState =
                serviceTemplateToUpdate.getServiceTemplateRegistrationState();
        if (registrationState == ServiceTemplateRegistrationState.APPROVED) {
            return updateServiceTemplateWhenRegistrationApproved(
                    ocl, serviceTemplateToUpdate, isRemoveFromCatalogUntilApproved);
        }
        return updateServiceTemplateWhenRegistrationNotApproved(ocl, serviceTemplateToUpdate);
    }

    private ServiceTemplateRequestHistoryEntity updateServiceTemplateWhenRegistrationApproved(
            Ocl ocl,
            ServiceTemplateEntity existingServiceTemplate,
            Boolean isRemoveFromCatalogUntilApproved) {
        checkAnyInProgressRequestForServiceTemplate(existingServiceTemplate);
        ServiceTemplateEntity serviceTemplateToUpdate = new ServiceTemplateEntity();
        BeanUtils.copyProperties(existingServiceTemplate, serviceTemplateToUpdate);
        updateServiceTemplateWithOcl(ocl, serviceTemplateToUpdate);
        boolean isAutoApprovedEnabled =
                isAutoApproveEnabledForCsp(serviceTemplateToUpdate.getCsp());
        ServiceTemplateRequestType requestType = ServiceTemplateRequestType.UPDATE;
        final ServiceTemplateRequestHistoryEntity storedUpdateHistory =
                createServiceTemplateHistory(
                        isAutoApprovedEnabled, requestType, serviceTemplateToUpdate);
        // if auto approved is enabled by CSP, approved service template with new ocl directly
        if (isAutoApprovedEnabled) {
            serviceTemplateToUpdate.setIsAvailableInCatalog(true);
            ServiceTemplateEntity updatedServiceTemplate =
                    templateStorage.storeAndFlush(serviceTemplateToUpdate);
            serviceTemplateOpenApiGenerator.updateServiceApi(updatedServiceTemplate);
        } else {
            existingServiceTemplate.setIsReviewInProgress(true);
            if (existingServiceTemplate.getIsAvailableInCatalog()
                    && isRemoveFromCatalogUntilApproved) {
                existingServiceTemplate.setIsAvailableInCatalog(false);
            }
            templateStorage.storeAndFlush(existingServiceTemplate);
        }
        return storedUpdateHistory;
    }

    private ServiceTemplateRequestHistoryEntity updateServiceTemplateWhenRegistrationNotApproved(
            Ocl ocl, ServiceTemplateEntity serviceTemplateToUpdate) {
        List<ServiceTemplateRequestHistoryEntity> oldRegisterRequestsInReview =
                serviceTemplateToUpdate.getServiceTemplateHistory().stream()
                        .filter(
                                history ->
                                        history.getRequestType()
                                                        == ServiceTemplateRequestType.REGISTER
                                                && history.getStatus()
                                                        == ServiceTemplateRequestStatus.IN_REVIEW)
                        .toList();
        if (!CollectionUtils.isEmpty(oldRegisterRequestsInReview)) {
            templateRequestStorage.cancelRequestsInBatch(oldRegisterRequestsInReview);
        }
        updateServiceTemplateWithOcl(ocl, serviceTemplateToUpdate);
        boolean isAutoApprovedEnabled =
                isAutoApproveEnabledForCsp(ocl.getCloudServiceProvider().getName());
        updateServiceTemplateRegistrationState(serviceTemplateToUpdate, isAutoApprovedEnabled);
        templateStorage.storeAndFlush(serviceTemplateToUpdate);
        ServiceTemplateRequestType requestType = ServiceTemplateRequestType.REGISTER;
        return createServiceTemplateHistory(
                isAutoApprovedEnabled, requestType, serviceTemplateToUpdate);
    }

    private void updateServiceTemplateRegistrationState(
            ServiceTemplateEntity serviceTemplateEntity, boolean isAutoApprovedEnabled) {

        if (isAutoApprovedEnabled) {
            serviceTemplateEntity.setServiceTemplateRegistrationState(
                    ServiceTemplateRegistrationState.APPROVED);
        } else {
            serviceTemplateEntity.setIsReviewInProgress(true);
            serviceTemplateEntity.setServiceTemplateRegistrationState(
                    ServiceTemplateRegistrationState.IN_REVIEW);
        }
        serviceTemplateEntity.setIsAvailableInCatalog(isAutoApprovedEnabled);
    }

    private void updateServiceTemplateWithOcl(Ocl ocl, ServiceTemplateEntity serviceTemplate) {
        validateRegions(ocl);
        validateFlavors(ocl);
        iconUpdate(serviceTemplate, ocl);
        checkParams(serviceTemplate, ocl);
        validateServiceDeployment(ocl.getDeployment(), serviceTemplate);
        billingConfigValidator.validateBillingConfig(ocl);
        if (Objects.nonNull(ocl.getServiceConfigurationManage())) {
            serviceConfigurationParameterValidator.validateServiceConfigurationParameters(ocl);
        }
        if (!CollectionUtils.isEmpty(ocl.getServiceActions())) {
            serviceActionTemplateValidator.validateServiceAction(ocl);
        }
        serviceTemplate.setOcl(ocl);
    }

    private void checkAnyInProgressRequestForServiceTemplate(
            ServiceTemplateEntity serviceTemplate) {
        if (!CollectionUtils.isEmpty(serviceTemplate.getServiceTemplateHistory())) {
            ServiceTemplateRequestHistoryEntity inProgressRequest =
                    serviceTemplate.getServiceTemplateHistory().stream()
                            .filter(
                                    request ->
                                            ServiceTemplateRequestStatus.IN_REVIEW
                                                    == request.getStatus())
                            .findFirst()
                            .orElse(null);
            if (Objects.nonNull(inProgressRequest)) {
                String errorMsg =
                        String.format(
                                "The request with id %s for the service template is waiting for"
                                        + " review. The new request is not allowed.",
                                inProgressRequest.getRequestId());
                log.error(errorMsg);
                throw new ServiceTemplateRequestNotAllowed(errorMsg);
            }
        }
    }

    private List<ServiceTemplateRequestHistoryEntity> getReviewPendingRequestsByServiceTemplateId(
            UUID serviceTemplateId) {
        ServiceTemplateRequestHistoryQueryModel queryModel =
                ServiceTemplateRequestHistoryQueryModel.builder()
                        .serviceTemplateId(serviceTemplateId)
                        .status(ServiceTemplateRequestStatus.IN_REVIEW)
                        .build();
        return templateRequestStorage.listServiceTemplateRequestHistoryByQueryModel(queryModel);
    }

    private void checkParams(ServiceTemplateEntity existingTemplate, Ocl ocl) {

        String oldCategory = existingTemplate.getCategory().name();
        String newCategory = ocl.getCategory().name();
        compare(oldCategory, newCategory, "category");

        String oldName = existingTemplate.getName();
        String newName = ocl.getName();
        compare(oldName, newName, "service name");

        String oldVersion = existingTemplate.getVersion();
        String newVersion = getSemverVersion(ocl.getServiceVersion()).getVersion();
        compare(oldVersion, newVersion, "service version");

        String oldCsp = existingTemplate.getCsp().name();
        String newCsp = ocl.getCloudServiceProvider().getName().name();
        compare(oldCsp, newCsp, "csp");

        String oldServiceHostingType = existingTemplate.getServiceHostingType().toValue();
        String newServiceHostingType = ocl.getServiceHostingType().toValue();
        compare(oldServiceHostingType, newServiceHostingType, "service hosting type");

        String oldServiceVendor = existingTemplate.getServiceVendor();
        String newServiceVendor = ocl.getServiceVendor();
        compare(oldServiceVendor, newServiceVendor, "serviceVendor");
    }

    private void compare(String oldParams, String newParams, String type) {
        if (!newParams.toLowerCase(Locale.ROOT).equals(oldParams.toLowerCase(Locale.ROOT))) {
            String errorMsg =
                    String.format(
                            "Update service failed, Value of %s cannot be "
                                    + "changed with an update request",
                            type);
            log.error(errorMsg);
            throw new ServiceTemplateRequestNotAllowed(errorMsg);
        }
    }

    private ServiceTemplateEntity createServiceTemplateEntity(Ocl ocl) {
        ServiceTemplateEntity newServiceTemplate = new ServiceTemplateEntity();
        newServiceTemplate.setId(UUID.randomUUID());
        newServiceTemplate.setCategory(ocl.getCategory());
        newServiceTemplate.setCsp(ocl.getCloudServiceProvider().getName());
        newServiceTemplate.setName(StringUtils.lowerCase(ocl.getName()));
        newServiceTemplate.setVersion(getSemverVersion(ocl.getServiceVersion()).getVersion());
        newServiceTemplate.setServiceHostingType(ocl.getServiceHostingType());
        newServiceTemplate.setServiceProviderContactDetails(ocl.getServiceProviderContactDetails());
        if (!userServiceHelper.isAuthEnable()) {
            newServiceTemplate.setServiceVendor(ocl.getServiceVendor());
        } else {
            String userManageIsv = userServiceHelper.getCurrentUserManageIsv();
            if (StringUtils.isNotEmpty(userManageIsv)
                    && !StringUtils.equals(ocl.getServiceVendor(), userManageIsv)) {
                throw new AccessDeniedException(
                        "No permissions to view or manage service template "
                                + "belonging to other serviceVendors.");
            }
            newServiceTemplate.setServiceVendor(ocl.getServiceVendor());
        }
        updateServiceTemplateWithOcl(ocl, newServiceTemplate);
        return newServiceTemplate;
    }

    private ServiceTemplateRequestHistoryEntity createServiceTemplateHistory(
            boolean isAutoApproveEnabled,
            ServiceTemplateRequestType requestType,
            ServiceTemplateEntity serviceTemplate) {
        ServiceTemplateRequestHistoryEntity serviceTemplateHistory =
                new ServiceTemplateRequestHistoryEntity();
        serviceTemplateHistory.setRequestId(UUID.randomUUID());
        serviceTemplateHistory.setServiceTemplate(serviceTemplate);
        serviceTemplateHistory.setOcl(serviceTemplate.getOcl());
        serviceTemplateHistory.setRequestType(requestType);
        serviceTemplateHistory.setBlockTemplateUntilReviewed(false);
        if (isAutoApproveEnabled) {
            serviceTemplateHistory.setStatus(ServiceTemplateRequestStatus.ACCEPTED);
            serviceTemplateHistory.setReviewComment(AUTO_APPROVED_REVIEW_COMMENT);
        } else {
            serviceTemplateHistory.setStatus(ServiceTemplateRequestStatus.IN_REVIEW);
        }
        return templateRequestStorage.storeAndFlush(serviceTemplateHistory);
    }

    private boolean isAutoApproveEnabledForCsp(Csp csp) {
        OrchestratorPlugin cspPlugin = pluginManager.getOrchestratorPlugin(csp);
        return cspPlugin.autoApproveServiceTemplateIsEnabled();
    }

    private Semver getSemverVersion(String serviceVersion) {
        try {
            return new Semver(serviceVersion);
        } catch (SemverException e) {
            String errorMsg =
                    String.format(
                            "The service version %s is a invalid semver version.", serviceVersion);
            throw new InvalidServiceVersionException(errorMsg);
        }
    }

    private void validateServiceVersion(
            String newServiceVersion, List<ServiceTemplateEntity> existingTemplates) {
        Semver newVersionSemver = getSemverVersion(newServiceVersion);
        if (!CollectionUtils.isEmpty(existingTemplates)) {
            ServiceTemplateEntity existingTemplateWithSameVersion =
                    existingTemplates.stream()
                            .filter(
                                    template ->
                                            newVersionSemver.isEqualTo(
                                                    new Semver(template.getVersion())))
                            .findAny()
                            .orElse(null);
            if (Objects.nonNull(existingTemplateWithSameVersion)) {
                String errorMsg =
                        String.format(
                                "Service template already registered with id %s. "
                                        + "The register request is not allowed.",
                                existingTemplateWithSameVersion.getId());
                log.error(errorMsg);
                throw new ServiceTemplateRequestNotAllowed(errorMsg);
            }
            Semver highestVersion =
                    existingTemplates.stream()
                            .map(serviceTemplate -> new Semver(serviceTemplate.getVersion()))
                            .sorted()
                            .toList()
                            .reversed()
                            .getFirst();
            if (!newVersionSemver.isGreaterThan(highestVersion)) {
                String errorMsg =
                        String.format(
                                "The version %s of service must be higher than the highest version"
                                        + " %s of the registered services with same name",
                                newServiceVersion, highestVersion);
                log.error(errorMsg);
                throw new InvalidServiceVersionException(errorMsg);
            }
        }
    }

    private void iconUpdate(ServiceTemplateEntity serviceTemplateEntity, Ocl ocl) {
        try {
            ocl.setIcon(IconProcessorUtil.processImage(ocl));
        } catch (Exception e) {
            ocl.setIcon(serviceTemplateEntity.getOcl().getIcon());
        }
    }

    private void validateFlavors(Ocl ocl) {
        List<String> errors = new ArrayList<>();
        // Check if service flavor names are unique
        Map<String, Long> nameCountMap =
                ocl.getFlavors().getServiceFlavors().stream()
                        .collect(
                                Collectors.groupingBy(
                                        ServiceFlavor::getName, Collectors.counting()));
        nameCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .forEach(
                        entry -> {
                            String message =
                                    String.format(
                                            "Duplicate flavor with name %s in service.",
                                            entry.getKey());
                            errors.add(message);
                        });
        if (!CollectionUtils.isEmpty(errors)) {
            throw new InvalidServiceFlavorsException(errors);
        }
    }

    private void validateRegions(Ocl ocl) {
        OrchestratorPlugin plugin =
                pluginManager.getOrchestratorPlugin(ocl.getCloudServiceProvider().getName());
        plugin.validateRegionsOfService(ocl);
    }

    private void validateServiceDeployment(
            Deployment deployment, ServiceTemplateEntity serviceTemplate) {
        AvailabilityZoneSchemaValidator.validateServiceAvailabilities(
                deployment.getServiceAvailabilityConfig());
        DeployVariableSchemaValidator.validateDeployVariable(deployment.getVariables());
        JsonObjectSchema jsonObjectSchema =
                serviceDeployVariablesJsonSchemaGenerator.buildDeployVariableJsonSchema(
                        deployment.getVariables());
        serviceTemplate.setJsonObjectSchema(jsonObjectSchema);
        validateTerraformScript(deployment);
    }

    /**
     * Get details of service template using id.
     *
     * @param serviceTemplateId the id of service template.
     * @param checkServiceVendor check the serviceVendor of the service template belonging to.
     * @param checkCsp check the cloud service provider of the service template.
     * @return Returns service template DB newTemplate.
     */
    public ServiceTemplateEntity getServiceTemplateDetails(
            UUID serviceTemplateId, boolean checkServiceVendor, boolean checkCsp) {
        ServiceTemplateEntity existingTemplate = getServiceTemplateById(serviceTemplateId);
        checkPermission(existingTemplate, checkServiceVendor, checkCsp);
        return existingTemplate;
    }

    /**
     * Search service templates with query model.
     *
     * @param query service template query model.
     * @return Returns list of service template newTemplate.
     */
    public List<ServiceTemplateEntity> listServiceTemplates(ServiceTemplateQueryModel query) {
        fillParamFromUserMetadata(query);
        return templateStorage.listServiceTemplates(query);
    }

    /**
     * Review service template registration.
     *
     * @param requestId the ID of service template request.
     * @param review the review.
     */
    public void reviewServiceTemplateRequest(UUID requestId, ReviewServiceTemplateRequest review) {
        ServiceTemplateRequestHistoryEntity existingTemplateRequest =
                templateRequestStorage.getEntityByRequestId(requestId);
        ServiceTemplateEntity serviceTemplate = existingTemplateRequest.getServiceTemplate();
        boolean hasManagePermissions =
                userServiceHelper.currentUserCanManageCsp(serviceTemplate.getCsp());
        if (!hasManagePermissions) {
            throw new AccessDeniedException(
                    "No permissions to review service template request "
                            + "belonging to other cloud service providers.");
        }
        if (ServiceTemplateRequestStatus.IN_REVIEW != existingTemplateRequest.getStatus()) {
            throw new ReviewServiceTemplateRequestNotAllowed(
                    "Service template request is not allowed to be reviewed.");
        }
        ServiceTemplateRequestHistoryEntity requestToReview =
                new ServiceTemplateRequestHistoryEntity();
        BeanUtils.copyProperties(existingTemplateRequest, requestToReview);
        if (ServiceReviewResult.APPROVED == review.getReviewResult()) {
            requestToReview.setStatus(ServiceTemplateRequestStatus.ACCEPTED);
        } else if (ServiceReviewResult.REJECTED == review.getReviewResult()) {
            requestToReview.setStatus(ServiceTemplateRequestStatus.REJECTED);
        }
        if (Objects.nonNull(review.getReviewComment())) {
            requestToReview.setReviewComment(review.getReviewComment());
        } else {
            requestToReview.setReviewComment(review.getReviewResult().toValue());
        }
        ServiceTemplateRequestHistoryEntity reviewedRequest =
                templateRequestStorage.storeAndFlush(requestToReview);
        updateServiceTemplateByReviewedRequest(reviewedRequest);
    }

    private void updateServiceTemplateByReviewedRequest(
            ServiceTemplateRequestHistoryEntity reviewedRequest) {
        ServiceTemplateEntity serviceTemplateToUpdate = new ServiceTemplateEntity();
        BeanUtils.copyProperties(reviewedRequest.getServiceTemplate(), serviceTemplateToUpdate);
        if (ServiceTemplateRequestStatus.ACCEPTED == reviewedRequest.getStatus()) {
            if (ServiceTemplateRequestType.REGISTER == reviewedRequest.getRequestType()) {
                serviceTemplateToUpdate.setServiceTemplateRegistrationState(
                        ServiceTemplateRegistrationState.APPROVED);
            } else if (ServiceTemplateRequestType.UPDATE == reviewedRequest.getRequestType()) {
                // When the update request is accepted, copy ocl in request into service template.
                updateServiceTemplateWithOcl(reviewedRequest.getOcl(), serviceTemplateToUpdate);
                serviceTemplateOpenApiGenerator.updateServiceApi(serviceTemplateToUpdate);
            }
            boolean isAvailableInCatalog =
                    ServiceTemplateRequestType.REMOVE_FROM_CATALOG
                            != reviewedRequest.getRequestType();
            serviceTemplateToUpdate.setIsAvailableInCatalog(isAvailableInCatalog);
        } else if (ServiceTemplateRequestStatus.REJECTED == reviewedRequest.getStatus()) {
            if (ServiceTemplateRequestType.REGISTER == reviewedRequest.getRequestType()) {
                serviceTemplateToUpdate.setServiceTemplateRegistrationState(
                        ServiceTemplateRegistrationState.REJECTED);
            }
        }
        updateIsReviewInProgressInServiceTemplate(serviceTemplateToUpdate);
        templateStorage.storeAndFlush(serviceTemplateToUpdate);
    }

    /**
     * Remove service template from catalog using id.
     *
     * @param id ID of service template.
     * @return Returns updated service template.
     */
    public ServiceTemplateRequestHistoryEntity removeFromCatalog(UUID id) {
        ServiceTemplateEntity existingTemplate = getServiceTemplateDetails(id, true, false);
        if (existingTemplate.getServiceTemplateRegistrationState()
                != ServiceTemplateRegistrationState.APPROVED) {
            String errMsg =
                    String.format(
                            "The registration of service template with id %s not approved. The"
                                    + " request to remove_from_catalog is not allowed.",
                            id);
            throw new ServiceTemplateRequestNotAllowed(errMsg);
        }
        checkAnyInProgressRequestForServiceTemplate(existingTemplate);
        // set isAvailableInCatalog to false directly.
        existingTemplate.setIsAvailableInCatalog(false);
        existingTemplate.setIsReviewInProgress(false);
        ServiceTemplateEntity updatedTemplate = templateStorage.storeAndFlush(existingTemplate);
        ServiceTemplateRequestType requestType = ServiceTemplateRequestType.REMOVE_FROM_CATALOG;
        // auto-approve the request to remove from catalog.
        return createServiceTemplateHistory(true, requestType, updatedTemplate);
    }

    /**
     * Re-register service template using the ID of service template.
     *
     * @param id ID of service template.
     * @return Returns updated service template.
     */
    public ServiceTemplateRequestHistoryEntity reAddToCatalog(UUID id) {
        ServiceTemplateEntity existingTemplate = getServiceTemplateDetails(id, true, false);
        if (existingTemplate.getServiceTemplateRegistrationState()
                != ServiceTemplateRegistrationState.APPROVED) {
            String errMsg =
                    String.format(
                            "The registration of service template with id %s not approved. The"
                                    + " request to re_add_to_catalog is not allowed.",
                            id);
            throw new ServiceTemplateRequestNotAllowed(errMsg);
        }
        checkAnyInProgressRequestForServiceTemplate(existingTemplate);
        boolean isAutoApprovedEnabled = isAutoApproveEnabledForCsp(existingTemplate.getCsp());
        if (isAutoApprovedEnabled) {
            existingTemplate.setIsAvailableInCatalog(true);
        } else {
            existingTemplate.setIsReviewInProgress(true);
        }
        ServiceTemplateEntity updatedTemplate = templateStorage.storeAndFlush(existingTemplate);
        ServiceTemplateRequestType requestType = ServiceTemplateRequestType.RE_ADD_TO_CATALOG;
        return createServiceTemplateHistory(isAutoApprovedEnabled, requestType, updatedTemplate);
    }

    /**
     * Delete service template using the ID of service template.
     *
     * @param id ID of service template.
     */
    public void deleteServiceTemplate(UUID id) {
        ServiceTemplateEntity existingTemplate = getServiceTemplateDetails(id, true, false);
        if (existingTemplate.getIsAvailableInCatalog()) {
            String errMsg =
                    String.format(
                            "Service template with id %s is still in catalog. The request to delete"
                                    + " is not allowed.",
                            id);
            throw new ServiceTemplateRequestNotAllowed(errMsg);
        }
        List<ServiceDeploymentEntity> deployServiceEntities = listDeployServicesByTemplateId(id);
        if (!CollectionUtils.isEmpty(deployServiceEntities)) {
            String errMsg =
                    String.format(
                            "Service template with id %s is still in use. The request to delete is"
                                    + " not allowed.",
                            id);
            log.error(errMsg);
            throw new ServiceTemplateRequestNotAllowed(errMsg);
        }
        templateStorage.deleteServiceTemplate(existingTemplate);
        serviceTemplateOpenApiGenerator.deleteServiceApi(id.toString());
    }

    /**
     * generate OpenApi for service template using the ID.
     *
     * @param id ID of service template.
     * @return path of openapi.html
     */
    public String getOpenApiUrl(UUID id) {
        String openApiUrl = serviceTemplateOpenApiGenerator.getOpenApi(getServiceTemplateById(id));
        if (StringUtils.isBlank(openApiUrl)) {
            throw new OpenApiFileGenerationException("Get openApi Url is Empty.");
        }
        return openApiUrl;
    }

    /**
     * Get service template history by service template id and other query parameters.
     *
     * @param serviceTemplateId id of service template.
     * @param requestType type of service template request.
     * @param changeStatus status of service template request.
     * @return list of service template request history.
     */
    public List<ServiceTemplateRequestHistory> getServiceTemplateRequestHistoryByServiceTemplateId(
            UUID serviceTemplateId,
            ServiceTemplateRequestType requestType,
            ServiceTemplateRequestStatus changeStatus) {
        ServiceTemplateEntity existingTemplate =
                getServiceTemplateDetails(serviceTemplateId, true, false);
        List<ServiceTemplateRequestHistoryEntity> historyList =
                existingTemplate.getServiceTemplateHistory();
        if (Objects.nonNull(requestType)) {
            historyList =
                    historyList.stream()
                            .filter(history -> history.getRequestType() == requestType)
                            .toList();
        }
        if (Objects.nonNull(changeStatus)) {
            historyList =
                    historyList.stream()
                            .filter(history -> history.getStatus() == changeStatus)
                            .toList();
        }
        return historyList.stream()
                .sorted(
                        Comparator.comparing(ServiceTemplateRequestHistoryEntity::getCreateTime)
                                .reversed())
                .map(EntityTransUtils::convertToServiceTemplateHistoryVo)
                .toList();
    }

    /**
     * List pending service template requests to review.
     *
     * @param queryModel query parameters.
     * @return list of service template requests to review.
     */
    public List<ServiceTemplateRequestToReview> getPendingServiceTemplateRequests(
            ServiceTemplateRequestHistoryQueryModel queryModel) {
        List<ServiceTemplateRequestHistoryEntity> reviewPendingRequests =
                templateRequestStorage.listServiceTemplateRequestHistoryByQueryModel(queryModel);
        return reviewPendingRequests.stream()
                .map(EntityTransUtils::convertToServiceTemplateRequestVo)
                .toList();
    }

    /**
     * Cancel service template request by request id.
     *
     * @param requestId id of service template request.
     */
    public void cancelServiceTemplateRequestByRequestId(UUID requestId) {
        ServiceTemplateRequestHistoryEntity requestToCancel =
                templateRequestStorage.getEntityByRequestId(requestId);
        checkPermission(requestToCancel.getServiceTemplate(), true, false);

        if (requestToCancel.getStatus() == ServiceTemplateRequestStatus.IN_REVIEW) {
            requestToCancel.setStatus(ServiceTemplateRequestStatus.CANCELLED);
            ServiceTemplateRequestHistoryEntity cancelledRequest =
                    templateRequestStorage.storeAndFlush(requestToCancel);
            ServiceTemplateEntity serviceTemplateToUpdate = requestToCancel.getServiceTemplate();
            if (cancelledRequest.getRequestType() == ServiceTemplateRequestType.REGISTER) {
                serviceTemplateToUpdate.setServiceTemplateRegistrationState(
                        ServiceTemplateRegistrationState.CANCELLED);
            }
            updateIsReviewInProgressInServiceTemplate(serviceTemplateToUpdate);
            templateStorage.storeAndFlush(serviceTemplateToUpdate);
        } else {
            throw new ServiceTemplateRequestNotAllowed(
                    "The request status is not in-review, the request is not allowed to cancel.");
        }
    }

    private void updateIsReviewInProgressInServiceTemplate(ServiceTemplateEntity serviceTemplate) {
        List<ServiceTemplateRequestHistoryEntity> reviewPendingRequests =
                getReviewPendingRequestsByServiceTemplateId(serviceTemplate.getId());
        serviceTemplate.setIsReviewInProgress(!CollectionUtils.isEmpty(reviewPendingRequests));
    }

    /**
     * Get ocl of service template request by requestId.
     *
     * @param requestId requestId of service template history.
     * @return ocl of service template request.
     */
    public ServiceTemplateRequestHistoryEntity getServiceTemplateRequestByRequestId(
            UUID requestId) {
        ServiceTemplateRequestHistoryEntity request =
                templateRequestStorage.getEntityByRequestId(requestId);
        checkPermission(request.getServiceTemplate(), true, false);
        return request;
    }

    private ServiceTemplateEntity getServiceTemplateById(UUID id) {
        return templateStorage.getServiceTemplateById(id);
    }

    private void checkPermission(
            ServiceTemplateEntity serviceTemplate, boolean checkServiceVendor, boolean checkCsp) {
        if (checkServiceVendor) {
            boolean hasManagePermissions =
                    userServiceHelper.currentUserCanManageIsv(serviceTemplate.getServiceVendor());
            if (!hasManagePermissions) {
                throw new AccessDeniedException(
                        "No permissions to view or manage service template belonging to other"
                                + " serviceVendors.");
            }
        }
        if (checkCsp) {
            boolean hasManagePermissions =
                    userServiceHelper.currentUserCanManageCsp(serviceTemplate.getCsp());
            if (!hasManagePermissions) {
                throw new AccessDeniedException(
                        "No permissions to review service template belonging to other cloud service"
                                + " providers.");
            }
        }
    }

    private List<ServiceDeploymentEntity> listDeployServicesByTemplateId(UUID serviceTemplateId) {
        ServiceQueryModel query = new ServiceQueryModel();
        query.setServiceTemplateId(serviceTemplateId);
        return serviceDeploymentStorage.listServices(query);
    }

    private void validateTerraformScript(Deployment deployment) {
        DeployerKind deployerKind = deployment.getDeployerTool().getKind();
        if (deployerKind == DeployerKind.TERRAFORM) {
            DeploymentScriptValidationResult tfValidationResult =
                    this.deployerKindManager.getDeployment(deployerKind).validate(deployment);
            if (!tfValidationResult.isValid()) {
                throw new TerraformScriptFormatInvalidException(
                        tfValidationResult.getDiagnostics().stream()
                                .map(DeployValidateDiagnostics::getDetail)
                                .collect(Collectors.toList()));
            }
        }

        if (deployerKind == DeployerKind.OPEN_TOFU) {
            DeploymentScriptValidationResult tfValidationResult =
                    this.deployerKindManager.getDeployment(deployerKind).validate(deployment);
            if (!tfValidationResult.isValid()) {
                throw new OpenTofuScriptFormatInvalidException(
                        tfValidationResult.getDiagnostics().stream()
                                .map(DeployValidateDiagnostics::getDetail)
                                .collect(Collectors.toList()));
            }
        }
    }

    private void fillParamFromUserMetadata(ServiceTemplateQueryModel query) {
        if (Objects.nonNull(query.getCheckServiceVendor()) && query.getCheckServiceVendor()) {
            String serviceVendor = userServiceHelper.getCurrentUserManageIsv();
            query.setServiceVendor(serviceVendor);
        }
    }
}
