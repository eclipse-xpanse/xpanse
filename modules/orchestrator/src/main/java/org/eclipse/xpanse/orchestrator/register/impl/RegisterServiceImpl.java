/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.register.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Implement Interface to manage register service entity in database.
 */
@Component
public class RegisterServiceImpl implements RegisterService {

    private final RegisterServiceStorage storage;
    private final OclLoader oclLoader;

    @Autowired
    public RegisterServiceImpl(RegisterServiceStorage registerServiceStorage, OclLoader oclLoader) {
        this.storage = registerServiceStorage;
        this.oclLoader = oclLoader;
    }

    /**
     * Update registered service using id and the ocl file url.
     *
     * @param id          id of the registered service.
     * @param oclLocation url of the ocl file.
     */
    @Override
    public void updateRegisteredServiceByUrl(String id, String oclLocation) throws Exception {
        Ocl ocl = oclLoader.getOcl(new URL(oclLocation));
        updateRegisteredService(id, ocl);
    }

    /**
     * Update registered service using id and the ocl model.
     *
     * @param id  id of the registered service.
     * @param ocl the Ocl model describing the register service.
     */
    @Override
    public void updateRegisteredService(String id, Ocl ocl) {
        RegisterServiceEntity existedService = storage.getRegisterServiceById(UUID.fromString(id));
        if (Objects.isNull(existedService)) {
            throw new IllegalArgumentException(String.format("Registered service with id %s not "
                    + "existed.", id));
        }
        existedService.setOcl(ocl);
        existedService.setServiceState(ServiceState.UPDATED);
        storage.store(existedService);
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

    /**
     * Register service using the ocl.
     *
     * @param ocl the Ocl model describing the register service.
     */
    @Override
    public UUID registerService(Ocl ocl) {
        RegisterServiceEntity newEntity = getNewRegisterServiceEntity(ocl);
        if (Objects.nonNull(storage.findRegisteredService(newEntity))) {
            throw new IllegalArgumentException("Service already registered.");
        }
        storage.store(newEntity);
        return newEntity.getId();
    }

    /**
     * Register service using the url of ocl file.
     *
     * @param oclLocation the url of the ocl file.
     */
    @Override
    public UUID registerServiceByUrl(String oclLocation) throws Exception {
        Ocl ocl = oclLoader.getOcl(new URL(oclLocation));
        return registerService(ocl);
    }

    /**
     * Get detail of registered service using ID.
     *
     * @param managedServiceId the ID of
     * @return registerServiceEntity
     */
    @Override
    public OclDetailVo getRegisteredService(String managedServiceId) {
        UUID uuid = UUID.fromString(managedServiceId);
        RegisterServiceEntity serviceEntity = storage.getRegisterServiceById(uuid);
        return convertToOclDetailVo(serviceEntity);
    }

    /**
     * Search registered service by query model.
     *
     * @param query the query model for search registered service.
     * @return list of OclDetailVo
     */
    @Override
    public List<OclDetailVo> queryRegisteredServices(RegisteredServiceQuery query) {
        List<RegisterServiceEntity> registeredServices = storage.queryRegisteredServices(query);
        if (CollectionUtils.isEmpty(registeredServices)) {
            return new ArrayList<>();
        }
        return registeredServices.stream().map(
                this::convertToOclDetailVo).collect(Collectors.toList());
    }

    /**
     * Search registered service tree by query model.
     *
     * @param query the query model for search registered service.
     * @return Returns Tree of RegisterServiceEntity
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
                    List<OclDetailVo> details = cspList.stream().map(this::convertToOclDetailVo)
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
     * @param managedServiceId ID of registered service.
     */
    @Override
    public void unregisterService(String managedServiceId) {
        UUID uuid = UUID.fromString(managedServiceId);
        storage.removeById(uuid);
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
