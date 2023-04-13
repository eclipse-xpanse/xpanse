/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.register.impl;

import jakarta.annotation.Resource;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.ServiceState;
import org.eclipse.xpanse.modules.models.query.RegisteredServiceQuery;
import org.eclipse.xpanse.modules.models.resource.Ocl;
import org.eclipse.xpanse.modules.models.utils.OclLoader;
import org.eclipse.xpanse.modules.models.view.CategoryOclVo;
import org.eclipse.xpanse.modules.models.view.OclDetailVo;
import org.eclipse.xpanse.modules.models.view.ProviderOclVo;
import org.eclipse.xpanse.modules.models.view.VersionOclVo;
import org.eclipse.xpanse.orchestrator.register.RegisterService;
import org.eclipse.xpanse.orchestrator.register.RegisterServiceStorage;
import org.eclipse.xpanse.orchestrator.utils.IconProcessorUtil;
import org.eclipse.xpanse.orchestrator.utils.OpenApiUtil;
import org.springframework.beans.BeanUtils;
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
    private OpenApiUtil openApiUtil;


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
            log.error("Registered service with id {} not existing.", id);
            throw new IllegalArgumentException(String.format("Registered service with id %s not "
                    + "existed.", id));
        }
        iconUpdate(existedService, ocl);
        checkParams(existedService, ocl);
        existedService.setOcl(ocl);
        existedService.setServiceState(ServiceState.UPDATED);
        storage.store(existedService);
        openApiUtil.updateServiceApi(existedService);
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
            throw new IllegalArgumentException(String.format(
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
        entity.setServiceState(ServiceState.REGISTERED);
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
        if (Objects.nonNull(storage.findRegisteredService(newEntity))) {
            log.error("Service already registered.");
            throw new IllegalArgumentException("Service already registered.");
        }
        storage.store(newEntity);
        openApiUtil.generateServiceApi(newEntity);
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
        return storage.getRegisterServiceById(uuid);
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
    public List<CategoryOclVo> queryRegisteredServicesTree(RegisteredServiceQuery query) {
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
                    List<OclDetailVo> details = cspList.stream()
                            .map(this::convertToOclDetailVo)
                            .collect(Collectors.toList());
                    providerOclVo.setDetails(details);
                    providerOclVo.setRegions(
                            details.get(0).getCloudServiceProvider().getRegions());
                    cspVoList.add(providerOclVo);
                });
                versionOclVo.setCloudProvider(cspVoList);
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
        openApiUtil.deleteServiceApi(registeredServiceId);
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
            throw new IllegalArgumentException(String.format("Registered service with id %s not "
                    + "existed.", id));
        }
        String openApiUrl = openApiUtil.getOpenApi(registerService);
        if (StringUtils.isBlank(openApiUrl)) {
            throw new RuntimeException("Get openApi Url is Empty.");
        }
        return openApiUrl;
    }

    private OclDetailVo convertToOclDetailVo(RegisterServiceEntity serviceEntity) {
        if (Objects.isNull(serviceEntity)) {
            return null;
        }
        OclDetailVo oclDetailVo = new OclDetailVo();
        oclDetailVo.setId(serviceEntity.getId());
        BeanUtils.copyProperties(serviceEntity.getOcl(), oclDetailVo);
        oclDetailVo.setCreateTime(serviceEntity.getCreateTime());
        oclDetailVo.setLastModifiedTime(serviceEntity.getLastModifiedTime());
        oclDetailVo.setServiceState(serviceEntity.getServiceState());
        return oclDetailVo;
    }


}
