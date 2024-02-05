/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.servicetemplate;

import jakarta.annotation.Nullable;
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
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.OpenApiFileGenerationException;
import org.eclipse.xpanse.modules.models.service.utils.ServiceVariablesJsonSchemaGenerator;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.OpenTofuScriptFormatInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateAlreadyRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateUpdateNotAllowed;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.TerraformScriptFormatInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidateDiagnostics;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.eclipse.xpanse.modules.security.common.CurrentUserInfo;
import org.eclipse.xpanse.modules.servicetemplate.utils.IconProcessorUtil;
import org.eclipse.xpanse.modules.servicetemplate.utils.ServiceTemplateOpenApiGenerator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Implement Interface to manage service template entity in database.
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
     * @return Returns service template DB entity.
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
     * @return Returns service template DB entity.
     */
    public ServiceTemplateEntity updateServiceTemplate(String id, Ocl ocl) {
        ServiceTemplateEntity existingService = storage.getServiceTemplateById(UUID.fromString(id));
        if (Objects.isNull(existingService)) {
            String errMsg = String.format("Service template with id %s not found.", id);
            log.error(errMsg);
            throw new ServiceTemplateNotRegistered(errMsg);
        }
        Optional<String> namespace = identityProviderManager.getUserNamespace();
        if (namespace.isEmpty() || !StringUtils.equals(namespace.get(),
                existingService.getNamespace())) {
            throw new AccessDeniedException("No permissions to update service templates "
                    + "belonging to other namespaces.");
        }
        iconUpdate(existingService, ocl);
        checkParams(existingService, ocl);
        validateTerraformScript(ocl);
        existingService.setOcl(ocl);
        existingService.setServiceRegistrationState(ServiceRegistrationState.UPDATED);
        JsonObjectSchema jsonObjectSchema =
                serviceVariablesJsonSchemaGenerator.buildJsonObjectSchema(
                        existingService.getOcl().getDeployment().getVariables());
        existingService.setJsonObjectSchema(jsonObjectSchema);
        ServiceTemplateEntity updatedServiceTemplate = storage.storeAndFlush(existingService);
        serviceTemplateOpenApiGenerator.updateServiceApi(updatedServiceTemplate);
        return updatedServiceTemplate;
    }

    private void checkParams(ServiceTemplateEntity existingService, Ocl ocl) {

        String oldCategory = existingService.getCategory().name();
        String newCategory = ocl.getCategory().name();
        compare(oldCategory, newCategory, "category");

        String oldName = existingService.getName();
        String newName = ocl.getName();
        compare(oldName, newName, "service name");

        String oldVersion = existingService.getVersion();
        String newVersion = ocl.getServiceVersion();
        compare(oldVersion, newVersion, "service version");

        String oldCsp = existingService.getCsp().name();
        String newCsp = ocl.getCloudServiceProvider().getName().name();
        compare(oldCsp, newCsp, "csp");

        String oldServiceHostingType = existingService.getServiceHostingType().toValue();
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
        ServiceTemplateEntity entity = new ServiceTemplateEntity();
        entity.setName(StringUtils.lowerCase(ocl.getName()));
        entity.setVersion(StringUtils.lowerCase(ocl.getServiceVersion()));
        entity.setCsp(ocl.getCloudServiceProvider().getName());
        entity.setCategory(ocl.getCategory());
        entity.setServiceHostingType(ocl.getServiceHostingType());
        entity.setOcl(ocl);
        entity.setServiceRegistrationState(ServiceRegistrationState.REGISTERED);
        entity.setServiceProviderContactDetails(ocl.getServiceProviderContactDetails());
        return entity;
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
     * @return Returns service template DB entity.
     */
    public ServiceTemplateEntity registerServiceTemplate(Ocl ocl) {
        ocl.setIcon(IconProcessorUtil.processImage(ocl));
        ServiceTemplateEntity newEntity = getNewServiceTemplateEntity(ocl);
        ServiceTemplateEntity existedEntity = storage.findServiceTemplate(newEntity);
        if (Objects.nonNull(existedEntity)) {
            String errorMsg =
                    String.format("Service already registered. service template id: %s.",
                            existedEntity.getId());
            log.error(errorMsg);
            throw new ServiceTemplateAlreadyRegistered(errorMsg);
        }
        JsonObjectSchema jsonObjectSchema =
                serviceVariablesJsonSchemaGenerator.buildJsonObjectSchema(
                        newEntity.getOcl().getDeployment().getVariables());
        validateTerraformScript(ocl);
        Optional<String> namespace = identityProviderManager.getUserNamespace();
        if (namespace.isPresent()) {
            newEntity.setNamespace(namespace.get());
        } else {
            newEntity.setNamespace(ocl.getNamespace());
        }
        newEntity.setJsonObjectSchema(jsonObjectSchema);
        ServiceTemplateEntity storedServiceTemplate = storage.storeAndFlush(newEntity);
        serviceTemplateOpenApiGenerator.generateServiceApi(storedServiceTemplate);
        return storedServiceTemplate;
    }

    /**
     * service template using the url of ocl file.
     *
     * @param oclLocation the url of the ocl file.
     * @return Returns service template DB entity.
     */
    public ServiceTemplateEntity registerServiceTemplateByUrl(String oclLocation) throws Exception {
        Ocl ocl = oclLoader.getOcl(URI.create(oclLocation).toURL());
        return registerServiceTemplate(ocl);
    }

    /**
     * Get detail of service template using ID.
     *
     * @param id the ID of
     * @return Returns service template DB entity.
     */
    public ServiceTemplateEntity getServiceTemplateDetails(String id, boolean checkNamespace) {
        UUID uuid = UUID.fromString(id);
        ServiceTemplateEntity existedService = storage.getServiceTemplateById(uuid);
        if (Objects.isNull(existedService)) {
            String errMsg = String.format("Service template with id %s not found.", id);
            log.error(errMsg);
            throw new ServiceTemplateNotRegistered(errMsg);
        }
        if (checkNamespace) {
            Optional<String> namespace = identityProviderManager.getUserNamespace();
            if (namespace.isEmpty() || !StringUtils.equals(namespace.get(),
                    existedService.getNamespace())) {
                throw new AccessDeniedException("No permissions to view details of service "
                        + "templates belonging to other namespaces.");
            }
        }
        return existedService;
    }

    /**
     * Search service template by query model.
     *
     * @param category           the category of the service template.
     * @param csp                the CSP of the service template.
     * @param serviceName        the serviceName of the service template.
     * @param serviceVersion     the serviceVersion of the service template.
     * @param serviceHostingType the serviceHostingType of the service template.
     * @return Returns list of service template entity.
     */
    public List<ServiceTemplateEntity> listServiceTemplates(Category category,
                                                            Csp csp,
                                                            String serviceName,
                                                            String serviceVersion,
                                                            ServiceHostingType serviceHostingType) {
        ServiceTemplateQueryModel query = getServiceTemplateQueryModel(
                category, csp, serviceName, serviceVersion, serviceHostingType
        );
        return storage.listServiceTemplates(query);
    }

    /**
     * Unregister service template using the ID of service template.
     *
     * @param id ID of service template.
     */
    public void unregisterServiceTemplate(String id) {
        ServiceTemplateEntity existedService = getServiceTemplateDetails(id, true);
        storage.removeById(existedService.getId());
        serviceTemplateOpenApiGenerator.deleteServiceApi(id);
    }

    /**
     * generate OpenApi for service template using the ID.
     *
     * @param id ID of service template.
     * @return path of openapi.html
     */
    public String getOpenApiUrl(String id) {
        UUID uuid = UUID.fromString(id);
        ServiceTemplateEntity serviceTemplate = storage.getServiceTemplateById(uuid);
        if (Objects.isNull(serviceTemplate) || Objects.isNull(serviceTemplate.getOcl())) {
            String errMsg = String.format("Service template with id %s not found.", id);
            log.error(errMsg);
            throw new ServiceTemplateNotRegistered(errMsg);
        }
        String openApiUrl = serviceTemplateOpenApiGenerator.getOpenApi(serviceTemplate);
        if (StringUtils.isBlank(openApiUrl)) {
            throw new OpenApiFileGenerationException("Get openApi Url is Empty.");
        }
        return openApiUrl;
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

    private ServiceTemplateQueryModel getServiceTemplateQueryModel(
            @Nullable Category category,
            @Nullable Csp csp,
            @Nullable String serviceName,
            @Nullable String serviceVersion,
            @Nullable ServiceHostingType serviceHostingType) {
        ServiceTemplateQueryModel query = new ServiceTemplateQueryModel();
        if (Objects.nonNull(category)) {
            query.setCategory(category);
        }
        if (Objects.nonNull(csp)) {
            query.setCsp(csp);
        }
        if (StringUtils.isNotBlank(serviceName)) {
            query.setServiceName(serviceName);
        }
        if (StringUtils.isNotBlank(serviceVersion)) {
            query.setServiceVersion(serviceVersion);
        }
        if (Objects.nonNull(serviceHostingType)) {
            query.setServiceHostingType(serviceHostingType);
        }
        CurrentUserInfo currentUserInfo = identityProviderManager.getCurrentUserInfo();
        if (Objects.nonNull(currentUserInfo)
                && StringUtils.isNotEmpty(currentUserInfo.getNamespace())) {
            query.setNamespace(currentUserInfo.getNamespace());
        }
        return query;
    }


}
