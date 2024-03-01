/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.servicetemplate;

import jakarta.annotation.Resource;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateQueryModel;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.deployment.DeployerKindManager;
import org.eclipse.xpanse.modules.models.common.exceptions.OpenApiFileGenerationException;
import org.eclipse.xpanse.modules.models.service.utils.ServiceVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ReviewRegistrationRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceReviewResult;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.OpenTofuScriptFormatInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateAlreadyRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateAlreadyReviewed;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateUpdateNotAllowed;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.TerraformScriptFormatInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidateDiagnostics;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.eclipse.xpanse.modules.security.common.CurrentUserInfo;
import org.eclipse.xpanse.modules.servicetemplate.utils.DeployVariableAutoFillValidator;
import org.eclipse.xpanse.modules.servicetemplate.utils.IconProcessorUtil;
import org.eclipse.xpanse.modules.servicetemplate.utils.ServiceTemplateOpenApiGenerator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Implement Interface to manage service template newTemplate in database.
 */
@Slf4j
@Service
public class ServiceTemplateManage {

    @Resource
    private ServiceTemplateStorage storage;
    @Resource
    private OclLoader oclLoader;
    @Resource
    private ServiceTemplateOpenApiGenerator serviceTemplateOpenApiGenerator;
    @Resource
    private IdentityProviderManager identityProviderManager;
    @Resource
    private ServiceVariablesJsonSchemaGenerator serviceVariablesJsonSchemaGenerator;
    @Resource
    private DeployerKindManager deployerKindManager;

    /**
     * Update service template using id and the ocl file url.
     *
     * @param id          id of the service template.
     * @param oclLocation url of the ocl file.
     * @return Returns service template DB newTemplate.
     */
    public ServiceTemplateEntity updateServiceTemplateByUrl(String id, String oclLocation)
            throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create(oclLocation).toURL());
        return updateServiceTemplate(id, ocl);
    }

    /**
     * Update service template using id and the ocl model.
     *
     * @param id  id of the service template.
     * @param ocl the Ocl model describing the service template.
     * @return Returns service template DB newTemplate.
     */
    public ServiceTemplateEntity updateServiceTemplate(String id, Ocl ocl) {
        ServiceTemplateEntity existingTemplate = getServiceTemplateDetails(id, true);
        iconUpdate(existingTemplate, ocl);
        checkParams(existingTemplate, ocl);
        validateTerraformScript(ocl);
        existingTemplate.setOcl(ocl);
        existingTemplate.setServiceRegistrationState(ServiceRegistrationState.APPROVAL_PENDING);
        JsonObjectSchema jsonObjectSchema =
                serviceVariablesJsonSchemaGenerator.buildJsonObjectSchema(
                        existingTemplate.getOcl().getDeployment().getVariables());
        existingTemplate.setJsonObjectSchema(jsonObjectSchema);
        ServiceTemplateEntity updatedServiceTemplate = storage.storeAndFlush(existingTemplate);
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
        String newVersion = ocl.getServiceVersion();
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
            log.error("Update service failed, Value of {} cannot be changed with an update request",
                    type);
            throw new ServiceTemplateUpdateNotAllowed(String.format(
                    "Update service failed, Value of %s be cannot changed with an update request",
                    type));
        }
    }

    private ServiceTemplateEntity getNewServiceTemplateEntity(Ocl ocl) {
        ServiceTemplateEntity newTemplate = new ServiceTemplateEntity();
        newTemplate.setName(StringUtils.lowerCase(ocl.getName()));
        newTemplate.setVersion(StringUtils.lowerCase(ocl.getServiceVersion()));
        newTemplate.setCsp(ocl.getCloudServiceProvider().getName());
        newTemplate.setCategory(ocl.getCategory());
        newTemplate.setServiceHostingType(ocl.getServiceHostingType());
        newTemplate.setOcl(ocl);
        newTemplate.setServiceRegistrationState(ServiceRegistrationState.APPROVAL_PENDING);
        newTemplate.setServiceProviderContactDetails(ocl.getServiceProviderContactDetails());
        return newTemplate;
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
        ocl.setIcon(IconProcessorUtil.processImage(ocl));
        ServiceTemplateEntity newTemplate = getNewServiceTemplateEntity(ocl);
        ServiceTemplateEntity existingTemplate = storage.findServiceTemplate(newTemplate);
        if (Objects.nonNull(existingTemplate)) {
            String errorMsg = String.format("Service template already registered with id %s",
                    existingTemplate.getId());
            log.error(errorMsg);
            throw new ServiceTemplateAlreadyRegistered(errorMsg);
        }
        DeployVariableAutoFillValidator.validateDeployVariableAutoFill(
                newTemplate.getOcl().getDeployment().getVariables());
        JsonObjectSchema jsonObjectSchema =
                serviceVariablesJsonSchemaGenerator.buildJsonObjectSchema(
                        newTemplate.getOcl().getDeployment().getVariables());
        validateTerraformScript(ocl);
        Optional<String> namespace = identityProviderManager.getUserNamespace();
        if (namespace.isPresent()) {
            newTemplate.setNamespace(namespace.get());
        } else {
            newTemplate.setNamespace(ocl.getNamespace());
        }
        newTemplate.setJsonObjectSchema(jsonObjectSchema);
        ServiceTemplateEntity storedServiceTemplate = storage.storeAndFlush(newTemplate);
        serviceTemplateOpenApiGenerator.generateServiceApi(storedServiceTemplate);
        return storedServiceTemplate;
    }

    /**
     * service template using the url of ocl file.
     *
     * @param oclLocation the url of the ocl file.
     * @return Returns service template DB newTemplate.
     */
    public ServiceTemplateEntity registerServiceTemplateByUrl(String oclLocation) throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create(oclLocation).toURL());
        return registerServiceTemplate(ocl);
    }

    /**
     * Get detail of service template using ID.
     *
     * @param id             the ID of
     * @param checkNamespace check the namespace of the service template belonging to.
     * @return Returns service template DB newTemplate.
     */
    public ServiceTemplateEntity getServiceTemplateDetails(String id, boolean checkNamespace)
            throws AccessDeniedException {
        ServiceTemplateEntity existingTemplate = getServiceTemplateById(id);
        if (checkNamespace) {
            Optional<String> namespace = identityProviderManager.getUserNamespace();
            if (namespace.isEmpty() || !StringUtils.equals(namespace.get(),
                    existingTemplate.getNamespace())) {
                throw new AccessDeniedException("No permissions to view or manage service template "
                        + "belonging to other namespaces.");
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
        return storage.listServiceTemplates(query);
    }

    /**
     * Review service template registration.
     *
     * @param id      ID of service template.
     * @param request the request of review registration.
     */
    public void reviewServiceTemplateRegistration(String id, ReviewRegistrationRequest request) {
        ServiceTemplateEntity existingTemplate = getServiceTemplateById(id);
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
        if (StringUtils.isNotBlank(request.getReviewComment())) {
            existingTemplate.setReviewComment(request.getReviewComment());
        }
        storage.storeAndFlush(existingTemplate);
    }


    /**
     * Unregister service template using the ID of service template.
     *
     * @param id ID of service template.
     */
    public void unregisterServiceTemplate(String id) {
        ServiceTemplateEntity existingTemplate = getServiceTemplateDetails(id, true);
        storage.removeById(existingTemplate.getId());
        serviceTemplateOpenApiGenerator.deleteServiceApi(id);
    }

    /**
     * generate OpenApi for service template using the ID.
     *
     * @param id ID of service template.
     * @return path of openapi.html
     */
    public String getOpenApiUrl(String id) {
        String openApiUrl = serviceTemplateOpenApiGenerator.getOpenApi(getServiceTemplateById(id));
        if (StringUtils.isBlank(openApiUrl)) {
            throw new OpenApiFileGenerationException("Get openApi Url is Empty.");
        }
        return openApiUrl;
    }

    private ServiceTemplateEntity getServiceTemplateById(String id) {
        UUID uuid = UUID.fromString(id);
        ServiceTemplateEntity serviceTemplate = storage.getServiceTemplateById(uuid);
        if (Objects.isNull(serviceTemplate) || Objects.isNull(serviceTemplate.getOcl())) {
            String errMsg = String.format("Service template with id %s not found.", id);
            log.error(errMsg);
            throw new ServiceTemplateNotRegistered(errMsg);
        }
        return serviceTemplate;
    }

    private void validateTerraformScript(Ocl ocl) {
        if (ocl.getDeployment().getKind() == DeployerKind.TERRAFORM) {
            DeploymentScriptValidationResult tfValidationResult =
                    this.deployerKindManager.getDeployment(ocl.getDeployment().getKind())
                            .validate(ocl);
            if (!tfValidationResult.isValid()) {
                throw new TerraformScriptFormatInvalidException(
                        tfValidationResult.getDiagnostics().stream()
                                .map(DeployValidateDiagnostics::getDetail)
                                .collect(Collectors.toList()));
            }
        }

        if (ocl.getDeployment().getKind() == DeployerKind.OPEN_TOFU) {
            DeploymentScriptValidationResult tfValidationResult =
                    this.deployerKindManager.getDeployment(ocl.getDeployment().getKind())
                            .validate(ocl);
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
            CurrentUserInfo currentUserInfo = identityProviderManager.getCurrentUserInfo();
            if (Objects.nonNull(currentUserInfo) && StringUtils.isNotEmpty(
                    currentUserInfo.getNamespace())) {
                query.setNamespace(currentUserInfo.getNamespace());
                log.info("Add parameter namespace with value {} to search service templates",
                        currentUserInfo.getNamespace());
            }
        }
    }


}
