/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.service;

import java.util.List;
import java.util.UUID;

/**
 * Interface for persist of DeployService.
 */
public interface DeployServiceStorage {

    /**
     * Add or update deployed service data to database.
     *
     * @param deployServiceEntity the entity of service.
     * @return deployServiceEntity the entity of service.
     */
    DeployServiceEntity storeAndFlush(DeployServiceEntity deployServiceEntity);

    /**
     * Method to get stored database entries by query model.
     *
     * @param query service query model.
     * @return Returns all rows from the service status database table.
     */
    List<DeployServiceEntity> listServices(ServiceQueryModel query);

    /**
     * Get detail of deployed service using ID.
     *
     * @param id the ID of deployed service.
     * @return registerServiceEntity
     */
    DeployServiceEntity findDeployServiceById(UUID id);

    /**
     * purge deployed service using service model.
     *
     * @param deployServiceEntity the model of deployed service.
     */
    void deleteDeployService(DeployServiceEntity deployServiceEntity);

}
