/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.service.impl;

import java.util.List;
import org.eclipse.xpanse.modules.database.service.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployResourceRepository;
import org.eclipse.xpanse.orchestrator.service.DeployResourceStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of the DeployResourceStorage.
 */
@Component
public class DatabaseDeployResourceStorage implements DeployResourceStorage {

    private final DeployResourceRepository deployResourceRepository;

    @Autowired
    public DatabaseDeployResourceStorage(DeployResourceRepository deployResourceRepository) {
        this.deployResourceRepository = deployResourceRepository;
    }

    /**
     * Add or update managed service data to database.
     *
     * @param deployResourceEntity the model of registered service.
     */
    @Override
    public void store(DeployResourceEntity deployResourceEntity) {
        this.deployResourceRepository.save(deployResourceEntity);
    }

    @Override
    public List<DeployResourceEntity> services() {
        return this.deployResourceRepository.findAll();
    }

}
