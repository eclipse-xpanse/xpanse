/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.servicetemplate;

import jakarta.annotation.Resource;
import java.net.URL;
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
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.models.common.exceptions.OpenApiFileGenerationException;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateAlreadyRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateUpdateNotAllowed;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.TerraformScriptFormatInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.query.ServiceTemplateQueryModel;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.CategoryOclVo;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ProviderOclVo;
import org.eclipse.xpanse.modules.models.servicetemplate.view.UserAvailableServiceVo;
import org.eclipse.xpanse.modules.models.servicetemplate.view.VersionOclVo;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidateDiagnostics;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidationResult;
import org.eclipse.xpanse.modules.servicetemplate.utils.IconProcessorUtil;
import org.eclipse.xpanse.modules.servicetemplate.utils.ServiceTemplateOpenApiGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    private DeployService deployService;


    /**
     * Update service template using id and the ocl file url.
     *
     * @param id          id of the service template.
     * @param oclLocation url of the ocl file.
     * @return Returns service template DB entity.
     */

    public ServiceTemplateEntity updateServiceTemplateByUrl(String id, String oclLocation)
            throws Exception {
        Ocl ocl = oclLoader.getOcl(new URL(oclLocation));
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
        ServiceTemplateEntity existedService = storage.getServiceTemplateById(UUID.fromString(id));
        if (Objects.isNull(existedService)) {
            String errMsg = String.format("Service template with id %s not found.", id);
            log.error(errMsg);
            throw new ServiceTemplateNotRegistered(errMsg);
        }
        iconUpdate(existedService, ocl);
        checkParams(existedService, ocl);
        validateTerraformScript(ocl);
        existedService.setOcl(ocl);
        existedService.setServiceRegistrationState(ServiceRegistrationState.UPDATED);
        storage.store(existedService);
        serviceTemplateOpenApiGenerator.updateServiceApi(existedService);
        return existedService;
    }

    private void checkParams(ServiceTemplateEntity existedService, Ocl ocl) {

        String oldCategory = existedService.getCategory().name();
        String newCategory = ocl.getCategory().name();
        compare(oldCategory, newCategory);

        String oldName = existedService.getName();
        String newName = ocl.getName();
        compare(oldName, newName);

        String oldVersion = existedService.getVersion();
        String newVersion = ocl.getServiceVersion();
        compare(oldVersion, newVersion);

        String oldCsp = existedService.getCsp().name();
        String newCsp = ocl.getCloudServiceProvider().getName().name();
        compare(oldCsp, newCsp);
    }

    private void compare(String oldParams, String newParams) {
        if (!newParams.toLowerCase(Locale.ROOT).equals(oldParams.toLowerCase(Locale.ROOT))) {
            log.error("Update service failed, Field {} cannot changed with update request",
                    oldParams);
            throw new ServiceTemplateUpdateNotAllowed(String.format(
                    "Update service failed, Field %s cannot changed with update request",
                    oldParams));
        }
    }

    private ServiceTemplateEntity getNewServiceTemplateEntity(Ocl ocl) {
        ServiceTemplateEntity entity = new ServiceTemplateEntity();
        entity.setName(StringUtils.lowerCase(ocl.getName()));
        entity.setVersion(StringUtils.lowerCase(ocl.getServiceVersion()));
        entity.setCsp(ocl.getCloudServiceProvider().getName());
        entity.setCategory(ocl.getCategory());
        entity.setOcl(ocl);
        entity.setServiceRegistrationState(ServiceRegistrationState.REGISTERED);
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
        validateTerraformScript(ocl);
        storage.store(newEntity);
        serviceTemplateOpenApiGenerator.generateServiceApi(newEntity);
        return newEntity;
    }

    /**
     * service template using the url of ocl file.
     *
     * @param oclLocation the url of the ocl file.
     * @return Returns service template DB entity.
     */

    public ServiceTemplateEntity registerServiceTemplateByUrl(String oclLocation) throws Exception {
        Ocl ocl = oclLoader.getOcl(new URL(oclLocation));
        return registerServiceTemplate(ocl);
    }

    /**
     * Get detail of service template using ID.
     *
     * @param id the ID of
     * @return Returns service template DB entity.
     */

    public ServiceTemplateEntity getServiceTemplateDetails(String id) {
        UUID uuid = UUID.fromString(id);
        ServiceTemplateEntity serviceTemplateEntity = storage.getServiceTemplateById(uuid);
        if (Objects.isNull(serviceTemplateEntity)) {
            String errMsg = String.format("Service template with id %s not found.", id);
            log.error(errMsg);
            throw new ServiceTemplateNotRegistered(errMsg);
        }
        return serviceTemplateEntity;
    }

    /**
     * Search service template by query model.
     *
     * @param query the query model for search service template.
     * @return Returns list of service template entity.
     */

    public List<ServiceTemplateEntity> listServiceTemplates(ServiceTemplateQueryModel query) {
        return storage.listServiceTemplates(query);
    }

    /**
     * Unregister service template using the ID of service template.
     *
     * @param serviceTemplateId ID of service template.
     */

    public void unregisterServiceTemplate(String serviceTemplateId) {
        UUID uuid = UUID.fromString(serviceTemplateId);
        storage.removeById(uuid);
        serviceTemplateOpenApiGenerator.deleteServiceApi(serviceTemplateId);
    }

    /**
     * Search registered service tree by query model.
     *
     * @param query the query model for search registered service.
     * @return Returns Tree of RegisterService View
     */
    public List<CategoryOclVo> getManagedServicesTree(ServiceTemplateQueryModel query) {
        List<ServiceTemplateEntity> serviceList = storage.listServiceTemplates(query);
        if (CollectionUtils.isEmpty(serviceList)) {
            return new ArrayList<>();
        }
        List<CategoryOclVo> oclTrees = new ArrayList<>();
        Map<String, List<ServiceTemplateEntity>> nameListMap =
                serviceList.stream().collect(Collectors.groupingBy(ServiceTemplateEntity::getName));
        nameListMap.forEach((name, nameList) -> {
            CategoryOclVo categoryOclVo = new CategoryOclVo();
            categoryOclVo.setName(name);
            List<VersionOclVo> versionVoList = new ArrayList<>();
            Map<String, List<ServiceTemplateEntity>> versionListMap =
                    nameList.stream()
                            .collect(Collectors.groupingBy(ServiceTemplateEntity::getVersion));
            versionListMap.forEach((version, versionList) -> {
                VersionOclVo versionOclVo = new VersionOclVo();
                versionOclVo.setVersion(version);
                List<ProviderOclVo> cspVoList = new ArrayList<>();
                Map<Csp, List<ServiceTemplateEntity>> cspListMap =
                        versionList.stream()
                                .collect(Collectors.groupingBy(ServiceTemplateEntity::getCsp));
                cspListMap.forEach((csp, cspList) -> {
                    ProviderOclVo providerOclVo = new ProviderOclVo();
                    providerOclVo.setName(csp);
                    List<UserAvailableServiceVo> details = cspList.stream()
                            .map(this::convertToUserAvailableServiceVo)
                            .collect(Collectors.toList());
                    providerOclVo.setDetails(details);
                    List<Region> regions = new ArrayList<>();
                    for (UserAvailableServiceVo userAvailableServiceVo : details) {
                        regions.addAll(userAvailableServiceVo.getRegions());
                    }
                    providerOclVo.setRegions(regions);
                    cspVoList.add(providerOclVo);
                });
                List<ProviderOclVo> sortedCspOclList =
                        cspVoList.stream().sorted(
                                        Comparator.comparingInt(o -> o.getName().ordinal()))
                                .collect(Collectors.toList());
                versionOclVo.setCloudProvider(sortedCspOclList);
                versionVoList.add(versionOclVo);
            });
            categoryOclVo.setVersions(versionVoList);
            oclTrees.add(categoryOclVo);
        });
        return oclTrees;
    }

    private UserAvailableServiceVo convertToUserAvailableServiceVo(
            ServiceTemplateEntity serviceEntity) {
        if (Objects.isNull(serviceEntity)) {
            return null;
        }
        UserAvailableServiceVo userAvailableServiceVo = new UserAvailableServiceVo();
        BeanUtils.copyProperties(serviceEntity, userAvailableServiceVo);
        userAvailableServiceVo.setIcon(serviceEntity.getOcl().getIcon());
        userAvailableServiceVo.setDescription(serviceEntity.getOcl().getDescription());
        userAvailableServiceVo.setNamespace(serviceEntity.getOcl().getNamespace());
        userAvailableServiceVo.setBilling(serviceEntity.getOcl().getBilling());
        userAvailableServiceVo.setFlavors(serviceEntity.getOcl().getFlavors());
        userAvailableServiceVo.setVariables(serviceEntity.getOcl().getDeployment().getVariables());
        userAvailableServiceVo.setDeployment(serviceEntity.getOcl().getDeployment());
        userAvailableServiceVo.setRegions(
                serviceEntity.getOcl().getCloudServiceProvider().getRegions());
        String openApiUrl = getOpenApiUrl(serviceEntity.getId().toString());
        userAvailableServiceVo.add(Link.of(openApiUrl, "openApi"));
        return userAvailableServiceVo;
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
            DeployValidationResult tfValidationResult =
                    this.deployService.getDeployment(ocl.getDeployment().getKind())
                            .validate(ocl);
            if (!tfValidationResult.isValid()) {
                throw new TerraformScriptFormatInvalidException(
                        tfValidationResult.getDiagnostics().stream()
                                .map(DeployValidateDiagnostics::getDetail)
                                .collect(Collectors.toList()));
            }
        }
    }

}
