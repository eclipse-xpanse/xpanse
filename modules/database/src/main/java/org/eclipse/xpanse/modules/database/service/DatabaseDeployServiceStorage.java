/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of the DeployServiceStorage.
 */
@Component
public class DatabaseDeployServiceStorage implements DeployServiceStorage {

    private final DeployServiceRepository deployServiceRepository;

    @Autowired
    public DatabaseDeployServiceStorage(DeployServiceRepository deployServiceRepository) {
        this.deployServiceRepository = deployServiceRepository;
    }

    /**
     * Store the entity to the database.
     *
     * @param deployServiceEntity the model of registered service.
     */
    @Override
    public void store(DeployServiceEntity deployServiceEntity) {
        this.deployServiceRepository.save(deployServiceEntity);
    }

    /**
     * Store the entity to the database and flush the data immediately.
     *
     * @param deployServiceEntity the model of registered service.
     */
    @Override
    public void storeAndFlush(DeployServiceEntity deployServiceEntity) {
        this.deployServiceRepository.saveAndFlush(deployServiceEntity);
    }

    @Override
    public List<DeployServiceEntity> services() {
        return this.deployServiceRepository.findAll();
    }

    /**
     * Get detail of deployed service using ID.
     *
     * @param id the ID of deployed service.
     * @return registerServiceEntity
     */
    @Override
    public DeployServiceEntity findDeployServiceById(UUID id) {
        Optional<DeployServiceEntity> optional =
                this.deployServiceRepository.findById(id);
        return optional.orElse(null);
    }
}
