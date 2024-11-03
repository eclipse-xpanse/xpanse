/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceorder;

import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;

/**
 * Interface for persist of ServiceModificationAudit.
 */
public interface ServiceOrderStorage {

    /**
     * Add or update service order data to database.
     *
     * @param entity the data of service order.
     * @return Returns the stored database entry.
     */
    ServiceOrderEntity storeAndFlush(ServiceOrderEntity entity);

    /**
     * Method to list database entry based ServiceOrderEntity.
     *
     * @param query query model for search service order entity.
     * @return Returns the database entry for the provided arguments.
     */
    List<ServiceOrderEntity> queryEntities(ServiceOrderEntity query);

    /**
     * Method to get database entry based ServiceOrderEntity.
     *
     * @param uuid uuid of ServiceOrderEntity.
     * @return Returns the database entry for the provided arguments.
     */
    ServiceOrderEntity getEntityById(UUID uuid);

    /**
     * Delete service order entity from database by entity.
     *
     * @param entity service order entity
     */
    void delete(ServiceOrderEntity entity);

    /**
     * Batch delete service order entities from database.
     *
     * @param entities service order entities
     */
    void deleteBatch(List<ServiceOrderEntity> entities);


    /**
     * Get deploy service entity by order id.
     *
     * @param uuid order id
     * @return DeployServiceEntity
     */
    ServiceDeploymentEntity getDeployServiceByOrderId(UUID uuid);

}
