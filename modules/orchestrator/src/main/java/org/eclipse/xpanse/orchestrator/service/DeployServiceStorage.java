/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.service;

import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;

/**
 * Interface for persist of DeployService.
 */
public interface DeployServiceStorage {

    /**
     * Add or update deployed service data to database.
     *
     * @param deployServiceEntity the model of deployed service.
     */
    void store(DeployServiceEntity deployServiceEntity);

    /**
     * Method to get all stored database entries.
     *
     * @return Returns all rows from the service status database table.
     */
    List<DeployServiceEntity> services();

    /**
     * Get detail of deployed service using ID.
     *
     * @param id the ID of deployed service.
     * @return registerServiceEntity
     */
    DeployServiceEntity findDeployServiceById(UUID id);
}
