/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.service;

import java.util.List;
import java.util.UUID;

/**
 * Interface for persist of serviceDeployment.
 */
public interface ServiceDeploymentStorage {

    /**
     * Add or update service deployment data to database.
     *
     * @param serviceDeploymentEntity the entity of service.
     * @return serviceDeploymentEntity the entity of service.
     */
    ServiceDeploymentEntity storeAndFlush(ServiceDeploymentEntity serviceDeploymentEntity);

    /**
     * Method to get stored database entries by query model.
     *
     * @param query service query model.
     * @return Returns all rows from the service status database table.
     */
    List<ServiceDeploymentEntity> listServices(ServiceQueryModel query);

    /**
     * Get detail of service deployment using ID.
     *
     * @param id the ID of deployed service.
     * @return serviceDeploymentEntity
     */
    ServiceDeploymentEntity findServiceDeploymentById(UUID id);

    /**
     * purge service deployment using service model.
     *
     * @param serviceDeploymentEntity the model of service deployment.
     */
    void deleteServiceDeployment(ServiceDeploymentEntity serviceDeploymentEntity);

}
