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
import org.eclipse.xpanse.modules.database.servicetemplatehistory.ServiceTemplateHistoryEntity;
import org.eclipse.xpanse.modules.database.servicetemplatehistory.ServiceTemplateHistoryStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.deployment.DeployerKindManager;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.OpenApiFileGenerationException;
import org.eclipse.xpanse.modules.models.service.utils.ServiceDeployVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ReviewRegistrationRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavor;
import org.eclipse.xpanse.modules.models.servicetemplate.change.ServiceTemplateHistoryVo;
import org.eclipse.xpanse.modules.models.servicetemplate.change.enums.ServiceTemplateChangeStatus;
import org.eclipse.xpanse.modules.models.servicetemplate.change.enums.ServiceTemplateRequestType;
import org.eclipse.xpanse.modules.models.servicetemplate.change.exceptions.ServiceTemplateChangeRequestNotAllowed;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceReviewResult;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidServiceFlavorsException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidServiceVersionException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.OpenTofuScriptFormatInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.TerraformScriptFormatInvalidException;
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
import org.eclipse.xpanse.modules.servicetemplate.utils.ServiceConfigurationParameterValidator;
import org.eclipse.xpanse.modules.servicetemplate.utils.ServiceTemplateOpenApiGenerator;
import org.semver4j.Semver;
import org.semver4j.SemverException;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Implement Interface to manage service template newTemplate in database.
 */
@Slf4j
@Service
public class ServiceTemplateManage {

    private static final String AUTO_APPROVED_REVIEW_COMMENT = "auto-approved by CSP";
    @Resource
    private ServiceTemplateStorage templateStorage;
    @Resource
    private ServiceTemplateHistoryStorage serviceTemplateHistoryStorage;
    @Resource
    private ServiceDeploymentStorage serviceDeploymentStorage;
    @Resource
    private ServiceTemplateOpenApiGenerator serviceTemplateOpenApiGenerator;
    @Resource
    private UserServiceHelper userServiceHelper;
    @Resource
    private ServiceDeployVariablesJsonSchemaGenerator serviceDeployVariablesJsonSchemaGenerator;
    @Resource
    private DeployerKindManager deployerKindManager;
    @Resource
    private BillingConfigValidator billingConfigValidator;
    @Resource
    private PluginManager pluginManager;
    @Resource
    private ServiceConfigurationParameterValidator serviceConfigurationParameterValidator;

    /**
     * Update service template using id and the ocl model.
     *
     * @param id                               id of the service template.
     * @param ocl                              the Ocl model describing the service template.
     * @param isRemoveFromCatalogUntilApproved If remove the service template from catalog
     *                                         until the updated one is approved.
     * @return Returns service template history entity.
     */
    public ServiceTemplateHistoryEntity updateServiceTemplate(
            UUID id, Ocl ocl, boolean isRemoveFromCatalogUntilApproved) {
        ServiceTemplateEntity existingServiceTemplate = getServiceTemplateDetails(id, true, false);
        ServiceTemplateRequestType requestType = ServiceTemplateRequestType.UPDATE;
        if (existingServiceTemplate.getIsUpdatePending()) {
            checkAnyInProgressRequestForServiceTemplate(existingServiceTemplate, requestType);
        }
        ServiceTemplateEntity serviceTemplateToUpdate = new ServiceTemplateEntity();
        BeanUtils.copyProperties(existingServiceTemplate, serviceTemplateToUpdate);
        iconUpdate(serviceTemplateToUpdate, ocl);
        checkParams(serviceTemplateToUpdate, ocl);
        validateRegions(ocl);
        validateFlavors(ocl);
        validateServiceDeployment(ocl.getDeployment(), serviceTemplateToUpdate);
        billingConfigValidator.validateBillingConfig(ocl);
        if (Objects.nonNull(serviceTemplateToUpdate.getOcl().getServiceConfigurationManage())
                && Objects.nonNull(ocl.getServiceConfigurationManage())) {
            serviceConfigurationParameterValidator.validateServiceConfigurationParameters(ocl);
        }
        serviceTemplateToUpdate.setOcl(ocl);
        boolean isAutoApprovedEnabled =
                isAutoApproveEnabledForCsp(ocl.getCloudServiceProvider().getName());
        final ServiceTemplateHistoryEntity storedUpdateHistory = createServiceTemplateHistory(
                isAutoApprovedEnabled, requestType, serviceTemplateToUpdate);
        // if auto approved is enabled by CSP, approved service template with new ocl directly
        if (isAutoApprovedEnabled) {
            serviceTemplateToUpdate.setAvailableInCatalog(true);
            serviceTemplateToUpdate.setIsUpdatePending(false);
            ServiceTemplateEntity updatedServiceTemplate =
                    templateStorage.storeAndFlush(serviceTemplateToUpdate);
            serviceTemplateOpenApiGenerator.updateServiceApi(updatedServiceTemplate);
        } else {
            existingServiceTemplate.setIsUpdatePending(true);
            if (existingServiceTemplate.getAvailableInCatalog()
                    && isRemoveFromCatalogUntilApproved) {
                existingServiceTemplate.setAvailableInCatalog(false);
            }
            templateStorage.storeAndFlush(existingServiceTemplate);
        }
        return storedUpdateHistory;
    }


    private void checkAnyInProgressRequestForServiceTemplate(
            ServiceTemplateEntity serviceTemplate, ServiceTemplateRequestType requestType) {
        if (CollectionUtils.isEmpty(serviceTemplate.getServiceTemplateHistory())) {
            return;
        }
        ServiceTemplateHistoryEntity inProgressHistory =
                serviceTemplate.getServiceTemplateHistory().stream()
                        .filter(history -> requestType == history.getRequestType()
                                && ServiceTemplateChangeStatus.IN_REVIEW == history.getStatus())
                        .findFirst()
                        .orElse(null);
        if (Objects.nonNull(inProgressHistory)) {
            String errorMsg = String.format("The same type request with change id %s for "
                            + "the service template with %s is waiting for review. "
                            + "The %s request is not allowed.",
                    serviceTemplate.getId(), inProgressHistory.getChangeId(),
                    requestType.toValue());
            log.error(errorMsg);
            throw new ServiceTemplateChangeRequestNotAllowed(errorMsg);
        }
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
    }

    private void compare(String oldParams, String newParams, String type) {
        if (!newParams.toLowerCase(Locale.ROOT).equals(oldParams.toLowerCase(Locale.ROOT))) {
            String errorMsg = String.format("Update service failed, Value of %s cannot be "
                    + "changed with an update request", type);
            log.error(errorMsg);
            throw new ServiceTemplateChangeRequestNotAllowed(errorMsg);
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
        newServiceTemplate.setOcl(ocl);
        newServiceTemplate.setServiceProviderContactDetails(ocl.getServiceProviderContactDetails());
        validateServiceDeployment(ocl.getDeployment(), newServiceTemplate);
        newServiceTemplate.setNamespace(userServiceHelper.getCurrentUserManageNamespace());
        return newServiceTemplate;
    }

    private ServiceTemplateHistoryEntity createServiceTemplateHistory(
            boolean isAutoApproveEnabled, ServiceTemplateRequestType requestType,
            ServiceTemplateEntity serviceTemplate) {
        ServiceTemplateHistoryEntity serviceTemplateHistory = new ServiceTemplateHistoryEntity();
        serviceTemplateHistory.setChangeId(UUID.randomUUID());
        serviceTemplateHistory.setServiceTemplate(serviceTemplate);
        serviceTemplateHistory.setOcl(serviceTemplate.getOcl());
        serviceTemplateHistory.setRequestType(requestType);
        serviceTemplateHistory.setBlockTemplateUntilReviewed(false);
        if (isAutoApproveEnabled) {
            serviceTemplateHistory.setStatus(ServiceTemplateChangeStatus.ACCEPTED);
            serviceTemplateHistory.setReviewComment(AUTO_APPROVED_REVIEW_COMMENT);
        } else {
            serviceTemplateHistory.setStatus(ServiceTemplateChangeStatus.IN_REVIEW);
        }
        return serviceTemplateHistoryStorage.storeAndFlush(serviceTemplateHistory);
    }

    private boolean isAutoApproveEnabledForCsp(Csp csp) {
        OrchestratorPlugin cspPlugin = pluginManager.getOrchestratorPlugin(csp);
        return cspPlugin.autoApproveServiceTemplateIsEnabled();
    }


    private Semver getSemverVersion(String serviceVersion) {
        try {
            return new Semver(serviceVersion);
        } catch (SemverException e) {
            String errorMsg = String.format("The service version %s is a invalid semver version.",
                    serviceVersion);
            throw new InvalidServiceVersionException(errorMsg);
        }
    }

    private void validateServiceVersion(Semver newServiceVersion,
                                        List<ServiceTemplateEntity> existingTemplates) {
        if (!CollectionUtils.isEmpty(existingTemplates)) {
            Semver highestVersion = existingTemplates.stream()
                    .map(serviceTemplate -> new Semver(serviceTemplate.getVersion())).sorted()
                    .toList().reversed().getFirst();
            if (!newServiceVersion.isGreaterThan(highestVersion)) {
                String errorMsg = String.format("The version %s of service must be higher than the"
                                + " highest version %s of the registered services with same name",
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

    /**
     * Register service template using the ocl.
     *
     * @param ocl the Ocl model describing the service template.
     * @return Returns service template history entity.
     */
    public ServiceTemplateHistoryEntity registerServiceTemplate(Ocl ocl) {
        Semver newServiceVersion = getSemverVersion(ocl.getServiceVersion());
        ServiceTemplateQueryModel queryModel =
                ServiceTemplateQueryModel.builder().category(ocl.getCategory())
                        .csp(ocl.getCloudServiceProvider().getName()).serviceName(ocl.getName())
                        .serviceHostingType(ocl.getServiceHostingType()).build();
        List<ServiceTemplateEntity> existingServiceTemplates =
                templateStorage.listServiceTemplates(queryModel);
        ServiceTemplateEntity existingTemplate = existingServiceTemplates.stream()
                .filter(template -> newServiceVersion.isEqualTo(new Semver(template.getVersion())))
                .findAny().orElse(null);
        if (Objects.nonNull(existingTemplate)) {
            String errorMsg = String.format("Service template already registered with id %s. "
                    + "The register request is not allowed.", existingTemplate.getId());
            log.error(errorMsg);
            throw new ServiceTemplateChangeRequestNotAllowed(errorMsg);
        }
        validateServiceVersion(newServiceVersion, existingServiceTemplates);
        ocl.setIcon(IconProcessorUtil.processImage(ocl));
        validateRegions(ocl);
        validateFlavors(ocl);
        billingConfigValidator.validateBillingConfig(ocl);
        if (Objects.nonNull(ocl.getServiceConfigurationManage())) {
            serviceConfigurationParameterValidator.validateServiceConfigurationParameters(ocl);
        }
        ServiceTemplateEntity serviceTemplateToRegister = createServiceTemplateEntity(ocl);
        boolean isAutoApprovedEnabled =
                isAutoApproveEnabledForCsp(serviceTemplateToRegister.getCsp());
        if (isAutoApprovedEnabled) {
            serviceTemplateToRegister.setServiceTemplateRegistrationState(
                    ServiceTemplateRegistrationState.APPROVED);
        } else {
            serviceTemplateToRegister.setServiceTemplateRegistrationState(
                    ServiceTemplateRegistrationState.IN_REVIEW);
        }
        serviceTemplateToRegister.setIsUpdatePending(false);
        serviceTemplateToRegister.setAvailableInCatalog(isAutoApprovedEnabled);
        ServiceTemplateEntity registeredServiceTemplate =
                templateStorage.storeAndFlush(serviceTemplateToRegister);
        ServiceTemplateHistoryEntity storedRegisterHistory =
                createServiceTemplateHistory(isAutoApprovedEnabled,
                        ServiceTemplateRequestType.REGISTER, registeredServiceTemplate);
        serviceTemplateOpenApiGenerator.generateServiceApi(registeredServiceTemplate);
        return storedRegisterHistory;
    }

    private void validateFlavors(Ocl ocl) {
        List<String> errors = new ArrayList<>();
        // Check if service flavor names are unique
        Map<String, Long> nameCountMap = ocl.getFlavors().getServiceFlavors().stream()
                .collect(Collectors.groupingBy(ServiceFlavor::getName, Collectors.counting()));
        nameCountMap.entrySet().stream().filter(entry -> entry.getValue() > 1).forEach(entry -> {
            String message =
                    String.format("Duplicate flavor with name %s in service.", entry.getKey());
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

    private void validateServiceDeployment(Deployment deployment,
                                           ServiceTemplateEntity serviceTemplate) {
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
     * Get detail of service template using ID.
     *
     * @param id             the ID of
     * @param checkNamespace check the namespace of the service template belonging to.
     * @param checkCsp       check the cloud service provider of the service template.
     * @return Returns service template DB newTemplate.
     */
    public ServiceTemplateEntity getServiceTemplateDetails(UUID id, boolean checkNamespace,
                                                           boolean checkCsp) {
        ServiceTemplateEntity existingTemplate = getServiceTemplateById(id);
        checkPermission(existingTemplate, checkNamespace, checkCsp);
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
     * @param id      ID of service template.
     * @param request the request of review registration.
     */
    public void reviewServiceTemplateRegistration(UUID id, ReviewRegistrationRequest request) {
        ServiceTemplateEntity existingTemplate = getServiceTemplateDetails(id, false, true);
        ServiceTemplateRegistrationState state =
                existingTemplate.getServiceTemplateRegistrationState();
        if (ServiceTemplateRegistrationState.APPROVED == state
                || ServiceTemplateRegistrationState.REJECTED == state) {
            String errMsg = String.format("Service template with id %s already reviewed.",
                    existingTemplate.getId());
            log.error(errMsg);
            throw new ServiceTemplateChangeRequestNotAllowed(errMsg);
        }
        if (ServiceReviewResult.APPROVED == request.getReviewResult()) {
            existingTemplate.setServiceTemplateRegistrationState(
                    ServiceTemplateRegistrationState.APPROVED);
            existingTemplate.setAvailableInCatalog(true);
        } else if (ServiceReviewResult.REJECTED == request.getReviewResult()) {
            existingTemplate.setServiceTemplateRegistrationState(
                    ServiceTemplateRegistrationState.REJECTED);
            existingTemplate.setAvailableInCatalog(false);
        }
        templateStorage.storeAndFlush(existingTemplate);
    }


    /**
     * Unregister service template using the ID of service template.
     *
     * @param id ID of service template.
     * @return Returns updated service template.
     */
    public ServiceTemplateHistoryEntity unregisterServiceTemplate(UUID id) {
        ServiceTemplateEntity existingTemplate = getServiceTemplateDetails(id, true, false);
        if (existingTemplate.getServiceTemplateRegistrationState()
                != ServiceTemplateRegistrationState.APPROVED) {
            String errMsg = String.format("Service template with id %s is not approved. "
                    + "The unregister request is not allowed.", existingTemplate.getId());
            throw new ServiceTemplateChangeRequestNotAllowed(errMsg);
        }
        ServiceTemplateRequestType requestType = ServiceTemplateRequestType.UNREGISTER;
        checkAnyInProgressRequestForServiceTemplate(existingTemplate, requestType);
        // set availableInCatalog to false directly.
        existingTemplate.setAvailableInCatalog(false);
        ServiceTemplateEntity updatedTemplate = templateStorage.storeAndFlush(existingTemplate);
        // no need a review for the unregister request, so auto-approve the unregister request.
        return createServiceTemplateHistory(true, requestType, updatedTemplate);
    }

    /**
     * Re-register service template using the ID of service template.
     *
     * @param id ID of service template.
     * @return Returns updated service template.
     */
    public ServiceTemplateHistoryEntity reRegisterServiceTemplate(UUID id) {
        ServiceTemplateEntity existingTemplate = getServiceTemplateDetails(id, true, false);
        if (existingTemplate.getServiceTemplateRegistrationState()
                != ServiceTemplateRegistrationState.APPROVED) {
            String errMsg = String.format("Service template with id %s is not approved. "
                    + "The re-register request is not allowed.", existingTemplate.getId());
            throw new ServiceTemplateChangeRequestNotAllowed(errMsg);
        }
        ServiceTemplateRequestType requestType = ServiceTemplateRequestType.RE_REGISTER;
        checkAnyInProgressRequestForServiceTemplate(existingTemplate, requestType);
        boolean isAutoApprovedEnabled = isAutoApproveEnabledForCsp(existingTemplate.getCsp());
        if (isAutoApprovedEnabled) {
            existingTemplate.setAvailableInCatalog(true);
        }
        ServiceTemplateEntity updatedTemplate = templateStorage.storeAndFlush(existingTemplate);
        return createServiceTemplateHistory(isAutoApprovedEnabled, requestType, updatedTemplate);
    }

    /**
     * Delete service template using the ID of service template.
     *
     * @param id ID of service template.
     */
    public void deleteServiceTemplate(UUID id) {
        ServiceTemplateEntity existingTemplate = getServiceTemplateDetails(id, true, false);
        if (existingTemplate.getAvailableInCatalog()) {
            String errMsg = String.format("Service template with id %s is not unregistered. "
                    + "The delete request is not allowed.", id);
            throw new ServiceTemplateChangeRequestNotAllowed(errMsg);
        }
        List<ServiceDeploymentEntity> deployServiceEntities =
                listDeployServicesByTemplateId(existingTemplate.getId());
        if (!deployServiceEntities.isEmpty()) {
            String errMsg = String.format("Service template with id %s is still in use. "
                    + "The delete request is not allowed.", id);
            log.error(errMsg);
            throw new ServiceTemplateChangeRequestNotAllowed(errMsg);
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
     * @param requestType       type of service template request.
     * @param changeStatus      status of service template request.
     * @return list of service template history.
     */
    public List<ServiceTemplateHistoryVo> getServiceTemplateHistoryByServiceTemplateId(
            UUID serviceTemplateId, ServiceTemplateRequestType requestType,
            ServiceTemplateChangeStatus changeStatus) {
        ServiceTemplateEntity existingTemplate =
                getServiceTemplateDetails(serviceTemplateId, true, false);
        List<ServiceTemplateHistoryEntity> historyList =
                existingTemplate.getServiceTemplateHistory();
        if (Objects.nonNull(requestType)) {
            historyList =
                    historyList.stream().filter(history -> history.getRequestType() == requestType)
                            .toList();
        }
        if (Objects.nonNull(changeStatus)) {
            historyList =
                    historyList.stream().filter(history -> history.getStatus() == changeStatus)
                            .toList();
        }
        return historyList.stream()
                .sorted(Comparator.comparing(ServiceTemplateHistoryEntity::getCreateTime)
                        .reversed()).map(EntityTransUtils::convertToServiceTemplateHistoryVo)
                .toList();
    }


    /**
     * Get ocl of service template request by changeId.
     *
     * @param changeId changeId of service template history.
     * @return ocl of service template request.
     */
    public Ocl getRequestedOclByChangeId(UUID changeId) {
        ServiceTemplateHistoryEntity history =
                serviceTemplateHistoryStorage.getEntityById(changeId);
        checkPermission(history.getServiceTemplate(), true, false);
        return history.getOcl();
    }

    private ServiceTemplateEntity getServiceTemplateById(UUID id) {
        return templateStorage.getServiceTemplateById(id);
    }

    private void checkPermission(ServiceTemplateEntity serviceTemplate, boolean checkNamespace,
                                 boolean checkCsp) {
        if (checkNamespace) {
            boolean hasManagePermissions =
                    userServiceHelper.currentUserCanManageNamespace(serviceTemplate.getNamespace());
            if (!hasManagePermissions) {
                throw new AccessDeniedException("No permissions to view or manage service template "
                        + "belonging to other namespaces.");
            }
        }
        if (checkCsp) {
            boolean hasManagePermissions =
                    userServiceHelper.currentUserCanManageCsp(serviceTemplate.getCsp());
            if (!hasManagePermissions) {
                throw new AccessDeniedException("No permissions to review service template "
                        + "belonging to other cloud service providers.");
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
        if (Objects.nonNull(query.getCheckNamespace()) && query.getCheckNamespace()) {
            String namespace = userServiceHelper.getCurrentUserManageNamespace();
            query.setNamespace(namespace);
        }
    }


}
