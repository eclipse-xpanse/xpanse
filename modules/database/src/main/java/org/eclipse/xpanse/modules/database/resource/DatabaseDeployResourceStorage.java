/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.resource;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of the DeployResourceStorage.
 */
@Component
public class DatabaseDeployResourceStorage implements DeployResourceStorage {

    private final DeployResourceRepository dployResourceRepository;

    @Autowired
    public DatabaseDeployResourceStorage(DeployResourceRepository dployResourceRepository) {
        this.dployResourceRepository = dployResourceRepository;
    }

    @Override
    public void deleteByDeployServiceId(UUID id) {
        dployResourceRepository.deleteByDeployServiceId(id);
    }
}
