/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.register.impl;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.ocl.loader.OclLoader;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.ServiceState;
import org.eclipse.xpanse.modules.ocl.loader.data.models.query.RegisteredServiceQuery;
import org.eclipse.xpanse.orchestrator.register.RegisterService;
import org.eclipse.xpanse.orchestrator.register.RegisterServiceStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    public void registerService(Ocl ocl) {
        RegisterServiceEntity newEntity = getNewRegisterServiceEntity(ocl);
        if (Objects.nonNull(storage.findRegisteredService(newEntity))) {
            throw new IllegalArgumentException("Service already registered.");
        }
        storage.store(newEntity);
    }

    /**
     * Register service using the url of ocl file.
     *
     * @param oclLocation the url of the ocl file.
     */
    @Override
    public void registerServiceByUrl(String oclLocation) throws Exception {
        Ocl ocl = oclLoader.getOcl(new URL(oclLocation));
        registerService(ocl);
    }

    /**
     * Get detail of registered service using ID.
     *
     * @param managedServiceId the ID of
     * @return registerServiceEntity
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
     * @return list of RegisterServiceEntity
     */
    @Override
    public List<RegisterServiceEntity> queryRegisteredServices(RegisteredServiceQuery query) {
        return storage.queryRegisteredServices(query);
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
}
