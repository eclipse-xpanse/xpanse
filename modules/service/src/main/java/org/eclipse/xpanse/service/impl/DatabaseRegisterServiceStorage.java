/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.database.register.RegisterServiceRepository;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.modules.ocl.loader.data.models.query.RegisterServiceQuery;
import org.eclipse.xpanse.service.RegisterServiceStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Bean to manage all service task to database.
 */
@Component
public class DatabaseRegisterServiceStorage implements RegisterServiceStorage {

    private final RegisterServiceRepository registerServiceRepository;

    @Autowired
    public DatabaseRegisterServiceStorage(RegisterServiceRepository registerServiceRepository) {
        this.registerServiceRepository = registerServiceRepository;
    }

    /**
     * Add or update managed service data to database.
     *
     * @param registerServiceEntity the model of registered service.
     */
    @Override
    public void store(RegisterServiceEntity registerServiceEntity) {
        registerServiceRepository.save(registerServiceEntity);
    }

    /**
     * Method to list database entry based registerServiceEntity.
     *
     * @param registerServiceEntity registerServiceEntity.
     * @return Returns the database entry for the provided arguments.
     */
    @Override
    public List<RegisterServiceEntity> listRegisterService(
            RegisterServiceEntity registerServiceEntity) {
        List<RegisterServiceEntity> serviceStatusEntities = services();
        return serviceStatusEntities.stream().filter(entity -> Objects.equals(registerServiceEntity,
                entity)).collect(
                Collectors.toList());
    }

    /**
     * Method to list database entry based registerServiceEntity by query model.
     *
     * @param registerServiceEntity registerServiceEntity.
     * @return Returns the database entry for the provided arguments.
     */
    @Override
    public RegisterServiceEntity getRegisterService(RegisterServiceEntity registerServiceEntity) {
        List<RegisterServiceEntity> serviceStatusEntities = services();
        for (RegisterServiceEntity serviceStatusEntity : serviceStatusEntities) {
            if (StringUtils.equals(getOclUniqueInfo(registerServiceEntity.getOcl()),
                    getOclUniqueInfo(serviceStatusEntity.getOcl()))) {
                return serviceStatusEntity;
            }
        }
        return null;

    }

    /**
     * Method to list database entry based registerServiceEntity.
     *
     * @param query query model for search register service entity.
     * @return Returns the database entry for the provided arguments.
     */
    @Override
    public List<RegisterServiceEntity> queryRegisterService(RegisterServiceQuery query) {
        boolean providerFilter = StringUtils.isNotBlank(query.getCspName());
        boolean serviceNameFilter = StringUtils.isNotBlank(query.getServiceName());
        boolean serviceVersionFilter = StringUtils.isNotBlank(query.getServiceVersion());
        List<RegisterServiceEntity> serviceStatusEntities = services();
        if (!(providerFilter || serviceNameFilter || serviceVersionFilter)) {
            return serviceStatusEntities;
        }
        List<RegisterServiceEntity> result = new ArrayList<>();

        for (RegisterServiceEntity serviceStatusEntity : serviceStatusEntities) {
            boolean isMatched = true;
            if (providerFilter) {
                isMatched = StringUtils.equalsIgnoreCase(query.getCspName(),
                        serviceStatusEntity.getOcl().getCloudServiceProvider().getName().name());
            }
            if (serviceNameFilter) {
                isMatched = isMatched && StringUtils.equalsIgnoreCase(query.getServiceName(),
                        serviceStatusEntity.getOcl().getName());
            }
            if (serviceVersionFilter) {
                isMatched = isMatched && StringUtils.equalsIgnoreCase(query.getServiceVersion(),
                        serviceStatusEntity.getOcl().getServiceVersion());
            }
            if (isMatched) {
                result.add(serviceStatusEntity);
            }
        }
        return result;
    }

    /**
     * Method to get database entry based registerServiceEntity.
     *
     * @param uuid uuid of registerServiceEntity.
     * @return Returns the database entry for the provided arguments.
     */
    @Override
    public RegisterServiceEntity getRegisterServiceById(UUID uuid) {
        Optional<RegisterServiceEntity> option = registerServiceRepository.findById(uuid);
        if (option.isPresent()) {
            return option.get();
        } else {
            throw new IllegalStateException(
                    String.format("Registered service %s not existed.", uuid));
        }
    }

    /**
     * Method to get all stored database entries.
     *
     * @return Returns all rows from the service status database table.
     */
    @Override
    public List<RegisterServiceEntity> services() {
        return registerServiceRepository.findAll();
    }

    /**
     * Remove register service entity from database by uuid.
     *
     * @param uuid uuid of register service entity
     */
    @Override
    public void removeById(UUID uuid) {
        if (registerServiceRepository.existsById(uuid)) {
            registerServiceRepository.deleteById(uuid);
        } else {
            throw new IllegalStateException(
                    String.format("Registered service %s not existed.", uuid));
        }

    }

    /**
     * Remove register service entity from database by entity.
     *
     * @param registerServiceEntity register service entity
     */
    @Override
    public void remove(RegisterServiceEntity registerServiceEntity) {
        registerServiceRepository.delete(registerServiceEntity);
    }


    private String getOclUniqueInfo(Ocl ocl) {
        StringBuilder stringBuilder = new StringBuilder();
        if (Objects.nonNull(ocl)) {
            stringBuilder.append(ocl.getName()).append("/");
            stringBuilder.append(ocl.getServiceVersion()).append("/");
            if (Objects.nonNull(ocl.getCloudServiceProvider())) {
                stringBuilder.append(
                        ocl.getCloudServiceProvider().getName().toValue());
            }
        }
        return stringBuilder.toString().toLowerCase(Locale.ROOT);

    }
}
