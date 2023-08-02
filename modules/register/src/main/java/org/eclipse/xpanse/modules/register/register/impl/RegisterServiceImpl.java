/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.register.register.impl;

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
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.database.register.RegisterServiceStorage;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.models.common.exceptions.OpenApiFileGenerationException;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.eclipse.xpanse.modules.models.service.register.Region;
import org.eclipse.xpanse.modules.models.service.register.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.service.register.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.service.register.exceptions.ServiceAlreadyRegisteredException;
import org.eclipse.xpanse.modules.models.service.register.exceptions.ServiceNotRegisteredException;
import org.eclipse.xpanse.modules.models.service.register.exceptions.ServiceUpdateNotAllowed;
import org.eclipse.xpanse.modules.models.service.register.exceptions.TerraformScriptFormatInvalidException;
import org.eclipse.xpanse.modules.models.service.register.query.RegisteredServiceQuery;
import org.eclipse.xpanse.modules.models.service.utils.OclLoader;
import org.eclipse.xpanse.modules.models.service.view.CategoryOclVo;
import org.eclipse.xpanse.modules.models.service.view.ProviderOclVo;
import org.eclipse.xpanse.modules.models.service.view.UserAvailableServiceVo;
import org.eclipse.xpanse.modules.models.service.view.VersionOclVo;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidateDiagnostics;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidationResult;
import org.eclipse.xpanse.modules.register.register.RegisterService;
import org.eclipse.xpanse.modules.register.register.utils.IconProcessorUtil;
import org.eclipse.xpanse.modules.register.register.utils.RegisteredServicesOpenApiGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Implement Interface to manage register service entity in database.
 */
@Slf4j
@Component
public class RegisterServiceImpl implements RegisterService {

    @Resource
    private RegisterServiceStorage storage;
    @Resource
    private OclLoader oclLoader;
    @Resource
    private RegisteredServicesOpenApiGenerator registeredServicesOpenApiGenerator;

    @Resource
    private DeployService deployService;


    /**
     * Update registered service using id and the ocl file url.
     *
     * @param id          id of the registered service.
     * @param oclLocation url of the ocl file.
     * @return Returns registered service DB entity.
     */
    @Override
    public RegisterServiceEntity updateRegisteredServiceByUrl(String id, String oclLocation)
            throws Exception {
        Ocl ocl = oclLoader.getOcl(new URL(oclLocation));
        return updateRegisteredService(id, ocl);
    }

    /**
     * Update registered service using id and the ocl model.
     *
     * @param id  id of the registered service.
     * @param ocl the Ocl model describing the register service.
     * @return Returns registered service DB entity.
     */
    @Override
    public RegisterServiceEntity updateRegisteredService(String id, Ocl ocl) {
        RegisterServiceEntity existedService = storage.getRegisterServiceById(UUID.fromString(id));
        if (Objects.isNull(existedService)) {
            String errMsg = String.format("Registered service with id %s not found.", id);
            log.error(errMsg);
            throw new ServiceNotRegisteredException(errMsg);
        }
        iconUpdate(existedService, ocl);
        checkParams(existedService, ocl);
        validateTerraformScript(ocl);
        existedService.setOcl(ocl);
        existedService.setServiceRegistrationState(ServiceRegistrationState.UPDATED);
        storage.store(existedService);
        registeredServicesOpenApiGenerator.updateServiceApi(existedService);
        return existedService;
    }

    private void checkParams(RegisterServiceEntity existedService, Ocl ocl) {

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
            throw new ServiceUpdateNotAllowed(String.format(
                    "Update service failed, Field %s cannot changed with update request",
                    oldParams));
        }
    }

    private RegisterServiceEntity getNewRegisterServiceEntity(Ocl ocl) {
        RegisterServiceEntity entity = new RegisterServiceEntity();
        entity.setName(StringUtils.lowerCase(ocl.getName()));
        entity.setVersion(StringUtils.lowerCase(ocl.getServiceVersion()));
        entity.setCsp(ocl.getCloudServiceProvider().getName());
        entity.setCategory(ocl.getCategory());
        entity.setOcl(ocl);
        entity.setServiceRegistrationState(ServiceRegistrationState.REGISTERED);
        return entity;
    }

    private void iconUpdate(RegisterServiceEntity registerService, Ocl ocl) {
        try {
            ocl.setIcon(IconProcessorUtil.processImage(ocl));
        } catch (Exception e) {
            ocl.setIcon(registerService.getOcl().getIcon());
        }
    }

    /**
     * Register service using the ocl.
     *
     * @param ocl the Ocl model describing the register service.
     * @return Returns registered service DB entity.
     */
    @Override
    public RegisterServiceEntity registerService(Ocl ocl) {
        ocl.setIcon(IconProcessorUtil.processImage(ocl));
        RegisterServiceEntity newEntity = getNewRegisterServiceEntity(ocl);
        RegisterServiceEntity existedEntity = storage.findRegisteredService(newEntity);
        if (Objects.nonNull(existedEntity)) {
            String errorMsg =
                    String.format("Service already registered. registered service id: %s.",
                            existedEntity.getId());
            log.error(errorMsg);
            throw new ServiceAlreadyRegisteredException(errorMsg);
        }
        validateTerraformScript(ocl);
        storage.store(newEntity);
        registeredServicesOpenApiGenerator.generateServiceApi(newEntity);
        return newEntity;
    }

    /**
     * Register service using the url of ocl file.
     *
     * @param oclLocation the url of the ocl file.
     * @return Returns registered service DB entity.
     */
    @Override
    public RegisterServiceEntity registerServiceByUrl(String oclLocation) throws Exception {
        Ocl ocl = oclLoader.getOcl(new URL(oclLocation));
        return registerService(ocl);
    }

    /**
     * Get detail of registered service using ID.
     *
     * @param managedServiceId the ID of
     * @return Returns registered service DB entity.
     */
    @Override
    public RegisterServiceEntity getRegisteredService(String managedServiceId) {
        UUID uuid = UUID.fromString(managedServiceId);
        RegisterServiceEntity registerServiceEntity = storage.getRegisterServiceById(uuid);
        if (Objects.isNull(registerServiceEntity)) {
            String errMsg = String.format("Registered service with id %s not found.",
                    managedServiceId);
            log.error(errMsg);
            throw new ServiceNotRegisteredException(errMsg);
        }
        return registerServiceEntity;
    }

    /**
     * Search registered service by query model.
     *
     * @param query the query model for search registered service.
     * @return Returns list of registered service entity.
     */
    @Override
    public List<RegisterServiceEntity> queryRegisteredServices(RegisteredServiceQuery query) {
        return storage.queryRegisteredServices(query);
    }

    /**
     * Search registered service tree by query model.
     *
     * @param query the query model for search registered service.
     * @return Returns Tree of RegisterService View
     */
    @Override
    public List<CategoryOclVo> getManagedServicesTree(RegisteredServiceQuery query) {
        List<RegisterServiceEntity> serviceList = storage.queryRegisteredServices(query);
        if (CollectionUtils.isEmpty(serviceList)) {
            return new ArrayList<>();
        }
        List<CategoryOclVo> oclTrees = new ArrayList<>();
        Map<String, List<RegisterServiceEntity>> nameListMap =
                serviceList.stream().collect(Collectors.groupingBy(RegisterServiceEntity::getName));
        nameListMap.forEach((name, nameList) -> {
            CategoryOclVo categoryOclVo = new CategoryOclVo();
            categoryOclVo.setName(name);
            List<VersionOclVo> versionVoList = new ArrayList<>();
            Map<String, List<RegisterServiceEntity>> versionListMap =
                    nameList.stream()
                            .collect(Collectors.groupingBy(RegisterServiceEntity::getVersion));
            versionListMap.forEach((version, versionList) -> {
                VersionOclVo versionOclVo = new VersionOclVo();
                versionOclVo.setVersion(version);
                List<ProviderOclVo> cspVoList = new ArrayList<>();
                Map<Csp, List<RegisterServiceEntity>> cspListMap =
                        versionList.stream()
                                .collect(Collectors.groupingBy(RegisterServiceEntity::getCsp));
                cspListMap.forEach((csp, cspList) -> {
                    ProviderOclVo providerOclVo = new ProviderOclVo();
                    providerOclVo.setName(csp);
                    List<UserAvailableServiceVo> details = cspList.stream()
                            .map(this::convertToRegisteredServiceUserVo)
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


    /**
     * Unregister service using the ID of registered service.
     *
     * @param registeredServiceId ID of registered service.
     */

    public void unregisterService(String registeredServiceId) {
        UUID uuid = UUID.fromString(registeredServiceId);
        storage.removeById(uuid);
        registeredServicesOpenApiGenerator.deleteServiceApi(registeredServiceId);
    }


    /**
     * generate OpenApi for registered service using the ID.
     *
     * @param id ID of registered service.
     * @return path of openapi.html
     */
    @Override
    public String getOpenApiUrl(String id) {
        UUID uuid = UUID.fromString(id);
        RegisterServiceEntity registerService = storage.getRegisterServiceById(uuid);
        if (Objects.isNull(registerService) || Objects.isNull(registerService.getOcl())) {
            String errMsg = String.format("Registered service with id %s not found.", id);
            log.error(errMsg);
            throw new ServiceNotRegisteredException(errMsg);
        }
        String openApiUrl = registeredServicesOpenApiGenerator.getOpenApi(registerService);
        if (StringUtils.isBlank(openApiUrl)) {
            throw new OpenApiFileGenerationException("Get openApi Url is Empty.");
        }
        return openApiUrl;
    }

    private UserAvailableServiceVo convertToRegisteredServiceUserVo(
            RegisterServiceEntity serviceEntity) {
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
