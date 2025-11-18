/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.servicetemplate;

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
import org.eclipse.xpanse.modules.database.utils.EntityTranslationUtils;
import org.eclipse.xpanse.modules.deployment.DeployerKindManager;
import org.eclipse.xpanse.modules.models.common.enums.UserOperation;
import org.eclipse.xpanse.modules.models.common.exceptions.OpenApiFileGenerationException;
import org.eclipse.xpanse.modules.models.service.utils.ServiceInputVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.InputVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ReviewServiceTemplateRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavor;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceReviewResult;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateReviewPluginResultType;
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
import org.eclipse.xpanse.modules.models.servicetemplate.utils.DeploymentVariableHelper;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.InputValidateDiagnostics;
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
import org.eclipse.xpanse.modules.servicetemplate.controller.ServiceControllerApiManage;
import org.eclipse.xpanse.modules.servicetemplate.price.BillingConfigValidator;
import org.eclipse.xpanse.modules.servicetemplate.utils.AvailabilityZoneSchemaValidator;
import org.eclipse.xpanse.modules.servicetemplate.utils.IconProcessorUtil;
import org.eclipse.xpanse.modules.servicetemplate.utils.InputVariablesSchemaValidator;
import org.eclipse.xpanse.modules.servicetemplate.utils.ServiceActionTemplateValidator;
import org.eclipse.xpanse.modules.servicetemplate.utils.ServiceConfigurationParameterValidator;
import org.eclipse.xpanse.modules.servicetemplate.utils.ServiceTemplateOpenApiGenerator;
import org.semver4j.Semver;
import org.semver4j.SemverException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/** Implement Interface to manage service template newTemplate in database. */
@Slf4j
@Service
public class ServiceTemplateManage {

    private static final String AUTO_APPROVED_REVIEW_COMMENT = "auto-approved by CSP";
    private final ServiceTemplateStorage templateStorage;
    private final ServiceTemplateRequestHistoryStorage templateRequestStorage;
    private final ServiceDeploymentStorage serviceDeploymentStorage;
    private final ServiceTemplateOpenApiGenerator serviceTemplateOpenApiGenerator;
    private final UserServiceHelper userServiceHelper;
    private final ServiceInputVariablesJsonSchemaGenerator serviceInputVariablesJsonSchemaGenerator;
    private final DeployerKindManager deployerKindManager;
    private final BillingConfigValidator billingConfigValidator;
    private final PluginManager pluginManager;
    private final ServiceConfigurationParameterValidator serviceConfigurationParameterValidator;
    private final ServiceActionTemplateValidator serviceActionTemplateValidator;
    private final ServiceControllerApiManage serviceControllerApiManage;

    /** Constructor method. */
    @Autowired
    public ServiceTemplateManage(
            ServiceTemplateStorage templateStorage,
            ServiceTemplateRequestHistoryStorage templateRequestStorage,
            ServiceDeploymentStorage serviceDeploymentStorage,
            ServiceTemplateOpenApiGenerator serviceTemplateOpenApiGenerator,
            UserServiceHelper userServiceHelper,
            ServiceInputVariablesJsonSchemaGenerator serviceInputVariablesJsonSchemaGenerator,
            DeployerKindManager deployerKindManager,
            BillingConfigValidator billingConfigValidator,
            PluginManager pluginManager,
            ServiceConfigurationParameterValidator serviceConfigurationParameterValidator,
            ServiceActionTemplateValidator serviceActionTemplateValidator,
            ServiceControllerApiManage serviceControllerApiManage) {
        this.templateStorage = templateStorage;
        this.templateRequestStorage = templateRequestStorage;
        this.serviceDeploymentStorage = serviceDeploymentStorage;
        this.serviceTemplateOpenApiGenerator = serviceTemplateOpenApiGenerator;
        this.userServiceHelper = userServiceHelper;
        this.serviceInputVariablesJsonSchemaGenerator = serviceInputVariablesJsonSchemaGenerator;
        this.deployerKindManager = deployerKindManager;
        this.billingConfigValidator = billingConfigValidator;
        this.pluginManager = pluginManager;
        this.serviceConfigurationParameterValidator = serviceConfigurationParameterValidator;
        this.serviceActionTemplateValidator = serviceActionTemplateValidator;
        this.serviceControllerApiManage = serviceControllerApiManage;
    }

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
        serviceControllerApiManage.generateServiceControllerOpenApiDoc(ocl);
        boolean isAutoApprovedEnabled = isAutoApproveEnabledForCsp(ocl);
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
     * @param isRemoveFromCatalogUntilApproved If remove from catalog until the updated one is
     *     approved.
     * @return Returns service template history entity.
     */
    public ServiceTemplateRequestHistoryEntity updateServiceTemplate(
            UUID id, Ocl ocl, boolean isRemoveFromCatalogUntilApproved) {
        UserOperation userOperation = UserOperation.UPDATE_SERVICE_TEMPLATE;
        ServiceTemplateEntity serviceTemplateToUpdate =
                getServiceTemplateDetails(id, userOperation, true, false);
        ServiceTemplateRegistrationState registrationState =
                serviceTemplateToUpdate.getServiceTemplateRegistrationState();
        serviceControllerApiManage.generateServiceControllerOpenApiDoc(ocl);
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
        boolean isAutoApprovedEnabled = isAutoApproveEnabledForCsp(ocl);
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
                                                && history.getRequestStatus()
                                                        == ServiceTemplateRequestStatus.IN_REVIEW)
                        .toList();
        if (!CollectionUtils.isEmpty(oldRegisterRequestsInReview)) {
            templateRequestStorage.cancelRequestsInBatch(oldRegisterRequestsInReview);
        }
        updateServiceTemplateWithOcl(ocl, serviceTemplateToUpdate);
        boolean isAutoApprovedEnabled = isAutoApproveEnabledForCsp(ocl);
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
                                                    == request.getRequestStatus())
                            .findFirst()
                            .orElse(null);
            if (Objects.nonNull(inProgressRequest)) {
                String errorMsg =
                        String.format(
                                "The request with id %s to the service template is waiting "
                                        + "for review. The new request is not allowed.",
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
                        .requestStatus(ServiceTemplateRequestStatus.IN_REVIEW)
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
        newServiceTemplate.setCategory(ocl.getCategory());
        newServiceTemplate.setCsp(ocl.getCloudServiceProvider().getName());
        newServiceTemplate.setName(StringUtils.lowerCase(ocl.getName()));
        newServiceTemplate.setVersion(getSemverVersion(ocl.getServiceVersion()).getVersion());
        newServiceTemplate.setServiceHostingType(ocl.getServiceHostingType());
        newServiceTemplate.setServiceProviderContactDetails(ocl.getServiceProviderContactDetails());
        newServiceTemplate.setShortCode(ocl.getShortCode());
        if (!userServiceHelper.isAuthEnable()) {
            newServiceTemplate.setServiceVendor(ocl.getServiceVendor());
        } else {
            String userManageIsv = userServiceHelper.getIsvManagedByCurrentUser();
            if (StringUtils.isNotEmpty(userManageIsv)
                    && !StringUtils.equals(ocl.getServiceVendor(), userManageIsv)) {
                String errorMsg =
                        String.format(
                                "No permission to %s owned by other service vendors.",
                                UserOperation.REGISTER_SERVICE_TEMPLATE.toValue());
                log.error(errorMsg);
                throw new AccessDeniedException(errorMsg);
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
        serviceTemplateHistory.setServiceTemplate(serviceTemplate);
        serviceTemplateHistory.setOcl(serviceTemplate.getOcl());
        serviceTemplateHistory.setRequestType(requestType);
        serviceTemplateHistory.setBlockTemplateUntilReviewed(false);
        if (isAutoApproveEnabled) {
            serviceTemplateHistory.setRequestStatus(ServiceTemplateRequestStatus.ACCEPTED);
            serviceTemplateHistory.setReviewComment(AUTO_APPROVED_REVIEW_COMMENT);
        } else {
            serviceTemplateHistory.setRequestStatus(ServiceTemplateRequestStatus.IN_REVIEW);
        }
        return templateRequestStorage.storeAndFlush(serviceTemplateHistory);
    }

    private boolean isAutoApproveEnabledForCsp(Ocl ocl) {
        OrchestratorPlugin cspPlugin =
                pluginManager.getOrchestratorPlugin(ocl.getCloudServiceProvider().getName());
        ServiceTemplateReviewPluginResultType cspReviewType =
                cspPlugin.validateServiceTemplate(ocl);
        if (cspReviewType == ServiceTemplateReviewPluginResultType.APPROVED) {
            cspPlugin.prepareServiceTemplate(ocl);
            return true;
        }
        return false;
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
        List<InputVariable> inputVariables = DeploymentVariableHelper.getInputVariables(deployment);
        InputVariablesSchemaValidator.validateInputVariables(inputVariables);
        JsonObjectSchema jsonObjectSchema =
                serviceInputVariablesJsonSchemaGenerator.buildJsonSchemaOfInputVariables(
                        inputVariables);
        serviceTemplate.setJsonObjectSchema(jsonObjectSchema);
        validateTerraformScript(deployment);
    }

    /**
     * Get details of service template using id.
     *
     * @param serviceTemplateId the id of service template.
     * @param userOperation the userOperation to the service template.
     * @param checkServiceVendor check the serviceVendor of belonging to.
     * @param checkCsp check the cloud service provider of the service template.
     * @return Returns service template DB newTemplate.
     */
    public ServiceTemplateEntity getServiceTemplateDetails(
            UUID serviceTemplateId,
            UserOperation userOperation,
            boolean checkServiceVendor,
            boolean checkCsp) {
        ServiceTemplateEntity existingTemplate = getServiceTemplateById(serviceTemplateId);
        checkPermission(existingTemplate, userOperation, checkServiceVendor, checkCsp);
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
    @Transactional
    public void reviewServiceTemplateRequest(UUID requestId, ReviewServiceTemplateRequest review) {
        ServiceTemplateRequestHistoryEntity existingTemplateRequest =
                templateRequestStorage.getEntityByRequestId(requestId);
        ServiceTemplateEntity serviceTemplate = existingTemplateRequest.getServiceTemplate();
        UserOperation userOperation = UserOperation.REVIEW_REQUEST_OF_SERVICE_TEMPLATE;
        checkPermission(serviceTemplate, userOperation, false, true);
        if (ServiceTemplateRequestStatus.IN_REVIEW != existingTemplateRequest.getRequestStatus()) {
            throw new ReviewServiceTemplateRequestNotAllowed(
                    "Service template request is not allowed to be reviewed.");
        }
        ServiceTemplateRequestHistoryEntity requestToReview =
                new ServiceTemplateRequestHistoryEntity();
        BeanUtils.copyProperties(existingTemplateRequest, requestToReview);
        if (ServiceReviewResult.APPROVED == review.getReviewResult()) {
            requestToReview.setRequestStatus(ServiceTemplateRequestStatus.ACCEPTED);
        } else if (ServiceReviewResult.REJECTED == review.getReviewResult()) {
            requestToReview.setRequestStatus(ServiceTemplateRequestStatus.REJECTED);
        }
        requestToReview.setReviewComment(review.getReviewComment());
        ServiceTemplateRequestHistoryEntity reviewedRequest =
                templateRequestStorage.storeAndFlush(requestToReview);
        updateServiceTemplateByReviewedRequest(reviewedRequest);
    }

    private void updateServiceTemplateByReviewedRequest(
            ServiceTemplateRequestHistoryEntity reviewedRequest) {
        ServiceTemplateEntity serviceTemplateToUpdate = new ServiceTemplateEntity();
        BeanUtils.copyProperties(reviewedRequest.getServiceTemplate(), serviceTemplateToUpdate);
        if (ServiceTemplateRequestStatus.ACCEPTED == reviewedRequest.getRequestStatus()) {
            if (ServiceTemplateRequestType.REGISTER == reviewedRequest.getRequestType()) {
                serviceTemplateToUpdate.setServiceTemplateRegistrationState(
                        ServiceTemplateRegistrationState.APPROVED);
            } else if (ServiceTemplateRequestType.UPDATE == reviewedRequest.getRequestType()) {
                // When the update request is accepted, copy ocl in request into service template.
                updateServiceTemplateWithOcl(reviewedRequest.getOcl(), serviceTemplateToUpdate);
                serviceTemplateOpenApiGenerator.updateServiceApi(serviceTemplateToUpdate);
            }
            boolean isAvailableInCatalog =
                    ServiceTemplateRequestType.UNPUBLISH != reviewedRequest.getRequestType();
            serviceTemplateToUpdate.setIsAvailableInCatalog(isAvailableInCatalog);
            OrchestratorPlugin cspPlugin =
                    pluginManager.getOrchestratorPlugin(serviceTemplateToUpdate.getCsp());
            cspPlugin.prepareServiceTemplate(reviewedRequest.getOcl());
        } else if (ServiceTemplateRequestStatus.REJECTED == reviewedRequest.getRequestStatus()) {
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
    public ServiceTemplateRequestHistoryEntity unpublish(UUID id) {
        UserOperation userOperation = UserOperation.UNPUBLISH_SERVICE_TEMPLATE;
        ServiceTemplateEntity existingTemplate =
                getServiceTemplateDetails(id, userOperation, true, false);
        if (existingTemplate.getServiceTemplateRegistrationState()
                != ServiceTemplateRegistrationState.APPROVED) {
            String errMsg =
                    String.format(
                            "The registration of service template with id %s not approved. The"
                                    + " request to unpublish is not allowed.",
                            id);
            throw new ServiceTemplateRequestNotAllowed(errMsg);
        }
        // set isAvailableInCatalog to false directly.
        existingTemplate.setIsAvailableInCatalog(false);
        updateIsReviewInProgressInServiceTemplate(existingTemplate);
        ServiceTemplateEntity updatedTemplate = templateStorage.storeAndFlush(existingTemplate);
        ServiceTemplateRequestType requestType = ServiceTemplateRequestType.UNPUBLISH;
        // auto-approve the request to remove from catalog.
        return createServiceTemplateHistory(true, requestType, updatedTemplate);
    }

    /**
     * Republish to catalog again.
     *
     * @param serviceTemplateId id of service template.
     * @return Returns updated service template.
     */
    public ServiceTemplateRequestHistoryEntity republish(UUID serviceTemplateId) {
        UserOperation userOperation = UserOperation.REPUBLISH_SERVICE_TEMPLATE;
        ServiceTemplateEntity existingTemplate =
                getServiceTemplateDetails(serviceTemplateId, userOperation, true, false);
        if (existingTemplate.getServiceTemplateRegistrationState()
                != ServiceTemplateRegistrationState.APPROVED) {
            String errMsg =
                    String.format(
                            "The registration of service template with id %s not approved. The"
                                    + " request to republish is not allowed.",
                            serviceTemplateId);
            throw new ServiceTemplateRequestNotAllowed(errMsg);
        }
        checkAnyInProgressRequestForServiceTemplate(existingTemplate);
        boolean isAutoApprovedEnabled = isAutoApproveEnabledForCsp(existingTemplate.getOcl());
        if (isAutoApprovedEnabled) {
            existingTemplate.setIsAvailableInCatalog(true);
        } else {
            existingTemplate.setIsReviewInProgress(true);
        }
        ServiceTemplateEntity updatedTemplate = templateStorage.storeAndFlush(existingTemplate);
        ServiceTemplateRequestType requestType = ServiceTemplateRequestType.REPUBLISH;
        return createServiceTemplateHistory(isAutoApprovedEnabled, requestType, updatedTemplate);
    }

    /**
     * Delete service template using the ID of service template.
     *
     * @param id ID of service template.
     */
    public void deleteServiceTemplate(UUID id) {
        UserOperation userOperation = UserOperation.DELETE_SERVICE_TEMPLATE;
        ServiceTemplateEntity existingTemplate =
                getServiceTemplateDetails(id, userOperation, true, false);
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
     * @param checkServiceVendor check service vendor.
     * @param checkCsp check csp.
     * @return list of service template request history.
     */
    @Transactional
    public List<ServiceTemplateRequestHistory> getServiceTemplateRequestHistoryByServiceTemplateId(
            UUID serviceTemplateId,
            ServiceTemplateRequestType requestType,
            ServiceTemplateRequestStatus changeStatus,
            boolean checkServiceVendor,
            boolean checkCsp) {
        UserOperation userOperation = UserOperation.VIEW_REQUEST_HISTORY_OF_SERVICE_TEMPLATE;
        ServiceTemplateEntity existingTemplate =
                getServiceTemplateDetails(
                        serviceTemplateId, userOperation, checkServiceVendor, checkCsp);
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
                            .filter(history -> history.getRequestStatus() == changeStatus)
                            .toList();
        }
        return historyList.stream()
                .sorted(
                        Comparator.comparing(ServiceTemplateRequestHistoryEntity::getCreatedTime)
                                .reversed())
                .map(EntityTranslationUtils::convertToServiceTemplateHistoryVo)
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
                .map(EntityTranslationUtils::convertToServiceTemplateRequestVo)
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
        UserOperation userOperation = UserOperation.CANCEL_REQUEST_OF_SERVICE_TEMPLATE;
        checkPermission(requestToCancel.getServiceTemplate(), userOperation, true, false);

        if (requestToCancel.getRequestStatus() == ServiceTemplateRequestStatus.IN_REVIEW) {
            requestToCancel.setRequestStatus(ServiceTemplateRequestStatus.CANCELLED);
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
        UserOperation userOperation = UserOperation.VIEW_REQUEST_DETAILS_OF_SERVICE_TEMPLATE;
        checkPermission(request.getServiceTemplate(), userOperation, true, false);
        return request;
    }

    private ServiceTemplateEntity getServiceTemplateById(UUID id) {
        return templateStorage.getServiceTemplateById(id);
    }

    private void checkPermission(
            ServiceTemplateEntity serviceTemplate,
            UserOperation userOperation,
            boolean checkServiceVendor,
            boolean checkCsp) {
        if (checkServiceVendor) {
            boolean hasManagePermissions =
                    userServiceHelper.currentUserCanManageIsv(serviceTemplate.getServiceVendor());
            if (!hasManagePermissions) {
                String errorMsg =
                        String.format(
                                "No permission to %s owned by other service vendors.",
                                userOperation.toValue());
                log.error(errorMsg);
                throw new AccessDeniedException(errorMsg);
            }
        }
        if (checkCsp) {
            boolean hasManagePermissions =
                    userServiceHelper.currentUserCanManageCsp(serviceTemplate.getCsp());
            if (!hasManagePermissions) {
                String errorMsg =
                        String.format(
                                "No permission to %s owned by other cloud service providers.",
                                userOperation.toValue());
                log.error(errorMsg);
                throw new AccessDeniedException(errorMsg);
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
                                .map(InputValidateDiagnostics::getDetail)
                                .collect(Collectors.toList()));
            }
        }

        if (deployerKind == DeployerKind.OPEN_TOFU) {
            DeploymentScriptValidationResult tfValidationResult =
                    this.deployerKindManager.getDeployment(deployerKind).validate(deployment);
            if (!tfValidationResult.isValid()) {
                throw new OpenTofuScriptFormatInvalidException(
                        tfValidationResult.getDiagnostics().stream()
                                .map(InputValidateDiagnostics::getDetail)
                                .collect(Collectors.toList()));
            }
        }
    }

    private void fillParamFromUserMetadata(ServiceTemplateQueryModel query) {
        if (Objects.nonNull(query.getCheckServiceVendor()) && query.getCheckServiceVendor()) {
            String serviceVendor = userServiceHelper.getIsvManagedByCurrentUser();
            query.setServiceVendor(serviceVendor);
        }
    }
}
