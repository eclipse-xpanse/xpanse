/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.servicetemplate;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.service.ServiceQueryModel;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateQueryModel;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.deployment.DeployerKindManager;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.OpenApiFileGenerationException;
import org.eclipse.xpanse.modules.models.service.utils.ServiceDeployVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ReviewRegistrationRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavor;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceReviewResult;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidServiceFlavorsException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.InvalidServiceVersionException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.OpenTofuScriptFormatInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateAlreadyRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateAlreadyReviewed;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateStillInUseException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateUpdateNotAllowed;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Implement Interface to manage service template newTemplate in database.
 */
@Slf4j
@Service
public class ServiceTemplateManage {

    @Resource
    private ServiceTemplateStorage templateStorage;
    @Resource
    private DeployServiceStorage deployServiceStorage;
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
     * @param id  id of the service template.
     * @param ocl the Ocl model describing the service template.
     * @return Returns service template DB newTemplate.
     */
    public ServiceTemplateEntity updateServiceTemplate(UUID id, Ocl ocl) {
        ServiceTemplateEntity existingTemplate = getServiceTemplateDetails(id, true, false);
        iconUpdate(existingTemplate, ocl);
        checkParams(existingTemplate, ocl);
        validateRegions(ocl);
        validateFlavors(ocl);
        billingConfigValidator.validateBillingConfig(ocl);
        if (Objects.nonNull(ocl.getServiceConfigurationManage())) {
            serviceConfigurationParameterValidator.validateServiceConfigurationParameters(ocl);
        }
        validateServiceDeployment(ocl.getDeployment(), existingTemplate);
        existingTemplate.setOcl(ocl);
        setServiceRegistrationState(existingTemplate);
        ServiceTemplateEntity updatedServiceTemplate =
                templateStorage.storeAndFlush(existingTemplate);
        serviceTemplateOpenApiGenerator.updateServiceApi(updatedServiceTemplate);
        return updatedServiceTemplate;
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
            throw new ServiceTemplateUpdateNotAllowed(errorMsg);
        }
    }

    private ServiceTemplateEntity getNewServiceTemplateEntity(Ocl ocl) {
        ServiceTemplateEntity newTemplate = new ServiceTemplateEntity();
        newTemplate.setId(UUID.randomUUID());
        newTemplate.setName(StringUtils.lowerCase(ocl.getName()));
        newTemplate.setVersion(getSemverVersion(ocl.getServiceVersion()).getVersion());
        newTemplate.setCsp(ocl.getCloudServiceProvider().getName());
        newTemplate.setCategory(ocl.getCategory());
        newTemplate.setServiceHostingType(ocl.getServiceHostingType());
        newTemplate.setOcl(ocl);
        newTemplate.setServiceProviderContactDetails(ocl.getServiceProviderContactDetails());
        setServiceRegistrationState(newTemplate);
        return newTemplate;
    }


    private void setServiceRegistrationState(ServiceTemplateEntity serviceTemplate) {
        Csp csp = serviceTemplate.getCsp();
        OrchestratorPlugin cspPlugin = pluginManager.getOrchestratorPlugin(csp);
        boolean cspAutoApproveIsEnabled = cspPlugin.autoApproveServiceTemplateIsEnabled();
        if (cspAutoApproveIsEnabled) {
            serviceTemplate.setServiceRegistrationState(ServiceRegistrationState.APPROVED);
            log.info("Service template {} managed by Csp {} auto approved",
                    serviceTemplate.getId(), csp);
        } else {
            serviceTemplate.setServiceRegistrationState(ServiceRegistrationState.APPROVAL_PENDING);
        }
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

    private void validateServiceVersion(Ocl ocl) {
        Semver newSemver = getSemverVersion(ocl.getServiceVersion());
        ServiceTemplateQueryModel query = new ServiceTemplateQueryModel(ocl.getCategory(),
                ocl.getCloudServiceProvider().getName(), ocl.getName(), null,
                ocl.getServiceHostingType(), null, false);
        List<ServiceTemplateEntity> templates = templateStorage.listServiceTemplates(query);
        if (!CollectionUtils.isEmpty(templates)) {
            Semver highestVersion = templates.stream()
                    .map(serviceTemplate -> new Semver(serviceTemplate.getVersion())).sorted()
                    .toList().reversed().getFirst();
            if (!newSemver.isGreaterThan(highestVersion)) {
                String errorMsg = String.format("The version %s of service must be higher than the"
                                + " highest version %s of the registered services with same name",
                        newSemver, highestVersion);
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
     * @return Returns service template DB newTemplate.
     */
    public ServiceTemplateEntity registerServiceTemplate(Ocl ocl) {
        ServiceTemplateEntity newTemplate = getNewServiceTemplateEntity(ocl);
        ServiceTemplateEntity existingTemplate = templateStorage.findServiceTemplate(newTemplate);
        if (Objects.nonNull(existingTemplate)) {
            String errorMsg = String.format("Service template already registered with id %s",
                    existingTemplate.getId());
            log.error(errorMsg);
            throw new ServiceTemplateAlreadyRegistered(errorMsg);
        }
        validateServiceVersion(ocl);
        validateRegions(ocl);
        validateFlavors(ocl);
        billingConfigValidator.validateBillingConfig(ocl);
        if (Objects.nonNull(ocl.getServiceConfigurationManage())) {
            serviceConfigurationParameterValidator.validateServiceConfigurationParameters(ocl);
        }
        validateServiceDeployment(ocl.getDeployment(), newTemplate);
        ocl.setIcon(IconProcessorUtil.processImage(ocl));
        String userManageNamespace =
                userServiceHelper.getCurrentUserManageNamespace();
        newTemplate.setNamespace(userManageNamespace);
        ServiceTemplateEntity storedServiceTemplate = templateStorage.storeAndFlush(newTemplate);
        serviceTemplateOpenApiGenerator.generateServiceApi(storedServiceTemplate);
        return storedServiceTemplate;
    }

    private void validateFlavors(Ocl ocl) {
        List<String> errors = new ArrayList<>();
        // Check if service flavor names are unique
        Map<String, Long> nameCountMap = ocl.getFlavors().getServiceFlavors().stream()
                .collect(Collectors.groupingBy(ServiceFlavor::getName, Collectors.counting()));
        nameCountMap.entrySet().stream().filter(entry -> entry.getValue() > 1)
                .forEach(entry -> {
                    String message = String.format("Duplicate flavor with name %s in service.",
                            entry.getKey());
                    errors.add(message);
                });
        if (!CollectionUtils.isEmpty(errors)) {
            throw new InvalidServiceFlavorsException(errors);
        }
    }

    private void validateRegions(Ocl ocl) {
        OrchestratorPlugin plugin = pluginManager.getOrchestratorPlugin(
                ocl.getCloudServiceProvider().getName());
        plugin.validateRegionsOfService(ocl);
    }

    private void validateServiceDeployment(Deployment deployment,
                                           ServiceTemplateEntity serviceTemplate) {
        AvailabilityZoneSchemaValidator.validateServiceAvailabilities(
                deployment.getServiceAvailabilityConfigs());
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
        if (checkNamespace) {
            boolean hasManagePermissions = userServiceHelper.currentUserCanManageNamespace(
                    existingTemplate.getNamespace());
            if (!hasManagePermissions) {
                throw new AccessDeniedException("No permissions to view or manage service template "
                        + "belonging to other namespaces.");
            }
        }
        if (checkCsp) {
            boolean hasManagePermissions = userServiceHelper.currentUserCanManageCsp(
                    existingTemplate.getCsp());
            if (!hasManagePermissions) {
                throw new AccessDeniedException("No permissions to review service template "
                        + "belonging to other cloud service providers.");
            }
        }
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
        if (ServiceRegistrationState.APPROVED == existingTemplate.getServiceRegistrationState()
                || ServiceRegistrationState.REJECTED
                == existingTemplate.getServiceRegistrationState()) {
            String errMsg = String.format("Service template with id %s already reviewed.",
                    existingTemplate.getId());
            log.error(errMsg);
            throw new ServiceTemplateAlreadyReviewed(errMsg);
        }
        if (ServiceReviewResult.APPROVED == request.getReviewResult()) {
            existingTemplate.setServiceRegistrationState(ServiceRegistrationState.APPROVED);
        } else if (ServiceReviewResult.REJECTED == request.getReviewResult()) {
            existingTemplate.setServiceRegistrationState(ServiceRegistrationState.REJECTED);
        }
        String reviewComment = StringUtils.isNotBlank(request.getReviewComment())
                ? request.getReviewComment() : request.getReviewResult().toValue();
        existingTemplate.setReviewComment(reviewComment);
        templateStorage.storeAndFlush(existingTemplate);
    }


    /**
     * Unregister service template using the ID of service template.
     *
     * @param id ID of service template.
     * @return Returns updated service template.
     */
    public ServiceTemplateEntity unregisterServiceTemplate(UUID id) {
        ServiceTemplateEntity existingTemplate = getServiceTemplateDetails(id, true, false);
        existingTemplate.setServiceRegistrationState(ServiceRegistrationState.UNREGISTERED);
        return templateStorage.storeAndFlush(existingTemplate);
    }

    /**
     * Re-register service template using the ID of service template.
     *
     * @param id ID of service template.
     * @return Returns updated service template.
     */
    public ServiceTemplateEntity reRegisterServiceTemplate(UUID id) {
        ServiceTemplateEntity existingTemplate = getServiceTemplateDetails(id, true, false);
        setServiceRegistrationState(existingTemplate);
        return templateStorage.storeAndFlush(existingTemplate);
    }

    /**
     * Delete service template using the ID of service template.
     *
     * @param id ID of service template.
     */
    public void deleteServiceTemplate(UUID id) {
        ServiceTemplateEntity existingTemplate = getServiceTemplateDetails(id, true, false);
        if (ServiceRegistrationState.UNREGISTERED
                != existingTemplate.getServiceRegistrationState()) {
            String errMsg = String.format("Service template with id %s is not unregistered.", id);
            log.error(errMsg);
            throw new ServiceTemplateStillInUseException(errMsg);
        }
        List<DeployServiceEntity> deployServiceEntities =
                listDeployServicesByTemplateId(existingTemplate.getId());
        if (!deployServiceEntities.isEmpty()) {
            String errMsg = String.format("Service template with id %s is still in use.", id);
            log.error(errMsg);
            throw new ServiceTemplateStillInUseException(errMsg);
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

    private ServiceTemplateEntity getServiceTemplateById(UUID id) {
        return templateStorage.getServiceTemplateById(id);
    }

    private List<DeployServiceEntity> listDeployServicesByTemplateId(UUID serviceTemplateId) {
        ServiceQueryModel query = new ServiceQueryModel();
        query.setServiceTemplateId(serviceTemplateId);
        return deployServiceStorage.listServices(query);
    }


    private void validateTerraformScript(Deployment deployment) {
        if (deployment.getKind() == DeployerKind.TERRAFORM) {
            DeploymentScriptValidationResult tfValidationResult =
                    this.deployerKindManager.getDeployment(deployment.getKind())
                            .validate(deployment);
            if (!tfValidationResult.isValid()) {
                throw new TerraformScriptFormatInvalidException(
                        tfValidationResult.getDiagnostics().stream()
                                .map(DeployValidateDiagnostics::getDetail)
                                .collect(Collectors.toList()));
            }
        }

        if (deployment.getKind() == DeployerKind.OPEN_TOFU) {
            DeploymentScriptValidationResult tfValidationResult =
                    this.deployerKindManager.getDeployment(deployment.getKind())
                            .validate(deployment);
            if (!tfValidationResult.isValid()) {
                throw new OpenTofuScriptFormatInvalidException(
                        tfValidationResult.getDiagnostics().stream()
                                .map(DeployValidateDiagnostics::getDetail)
                                .collect(Collectors.toList()));
            }
        }
    }

    private void fillParamFromUserMetadata(ServiceTemplateQueryModel query) {
        if (query.isCheckNamespace()) {
            String namespace = userServiceHelper.getCurrentUserManageNamespace();
            query.setNamespace(namespace);
        }
    }


}
