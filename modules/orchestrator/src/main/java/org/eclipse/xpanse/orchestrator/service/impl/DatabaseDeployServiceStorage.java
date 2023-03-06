/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.service.impl;

import java.util.List;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceRepository;
import org.eclipse.xpanse.orchestrator.service.DeployServiceStorage;
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
     * Add or update managed service data to database.
     *
     * @param deployServiceEntity the model of registered service.
     */
    @Override
    public void store(DeployServiceEntity deployServiceEntity) {
        this.deployServiceRepository.save(deployServiceEntity);
    }

    @Override
    public List<DeployServiceEntity> services() {
        return this.deployServiceRepository.findAll();
    }

}
