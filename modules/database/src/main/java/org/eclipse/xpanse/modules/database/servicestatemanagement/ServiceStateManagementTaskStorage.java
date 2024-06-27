/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicestatemanagement;

import java.util.List;
import java.util.UUID;

/**
 * Interface for persist of ServiceManagementTask.
 */
public interface ServiceStateManagementTaskStorage {

    /**
     * Add or update managed service data to database.
     *
     * @param taskEntity the data of management task.
     * @return Returns the stored database entry.
     */
    ServiceStateManagementTaskEntity storeAndFlush(ServiceStateManagementTaskEntity taskEntity);

    /**
     * Method to list database entry based ServiceStateManagementTaskEntity.
     *
     * @param query query model for search management task entity.
     * @return Returns the database entry for the provided arguments.
     */
    List<ServiceStateManagementTaskEntity> queryTasks(ServiceStateManagementTaskEntity query);

    /**
     * Method to get database entry based ServiceStateManagementTaskEntity.
     *
     * @param uuid uuid of ServiceStateManagementTaskEntity.
     * @return Returns the database entry for the provided arguments.
     */
    ServiceStateManagementTaskEntity getTaskById(UUID uuid);

    /**
     * Remove management task entity from database by entity.
     *
     * @param taskEntity management task entity
     */
    void delete(ServiceStateManagementTaskEntity taskEntity);

    /**
     * Batch remove management task entities from database.
     *
     * @param taskEntities management task entities
     */
    void deleteBatch(List<ServiceStateManagementTaskEntity> taskEntities);

}
