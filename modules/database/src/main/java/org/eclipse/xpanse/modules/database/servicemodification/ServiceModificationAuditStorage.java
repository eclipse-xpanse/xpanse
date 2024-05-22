/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicemodification;

import java.util.List;
import java.util.UUID;

/**
 * Interface for persist of ServiceModificationAudit.
 */
public interface ServiceModificationAuditStorage {

    /**
     * Add or update managed service data to database.
     *
     * @param entity the data of service modification audit.
     * @return Returns the stored database entry.
     */
    ServiceModificationAuditEntity storeAndFlush(ServiceModificationAuditEntity entity);

    /**
     * Method to list database entry based ServiceModificationAuditEntity.
     *
     * @param query query model for search service modification audit entity.
     * @return Returns the database entry for the provided arguments.
     */
    List<ServiceModificationAuditEntity> queryEntities(ServiceModificationAuditEntity query);

    /**
     * Method to get database entry based ServiceModificationAuditEntity.
     *
     * @param uuid uuid of ServiceModificationAuditEntity.
     * @return Returns the database entry for the provided arguments.
     */
    ServiceModificationAuditEntity getEntityById(UUID uuid);

    /**
     * Remove service modification audit entity from database by entity.
     *
     * @param entity service modification audit entity
     */
    void remove(ServiceModificationAuditEntity entity);

    /**
     * Batch remove service modification audit entities from database.
     *
     * @param entities service modification audit entities
     */
    void batchRemove(List<ServiceModificationAuditEntity> entities);

}
