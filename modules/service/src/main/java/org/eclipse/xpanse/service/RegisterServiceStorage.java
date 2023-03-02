/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.service;

import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.ocl.loader.data.models.query.RegisterServiceQuery;

/**
 * Interface to be implemented by register service database.
 */
public interface RegisterServiceStorage {

    /**
     * Add or update managed service data to database.
     *
     * @param registerServiceEntity the model of registered service.
     */
    void store(RegisterServiceEntity registerServiceEntity);

    /**
     * Method to list database entry based registerServiceEntity.
     *
     * @param registerServiceEntity registerServiceEntity.
     * @return Returns the database entry for the provided arguments.
     */
    List<RegisterServiceEntity> listRegisterService(RegisterServiceEntity registerServiceEntity);

    /**
     * Method to list database entry based registerServiceEntity.
     *
     * @param query query model for search register service entity.
     * @return Returns the database entry for the provided arguments.
     */
    List<RegisterServiceEntity> queryRegisterService(RegisterServiceQuery query);

    /**
     * Method to get database entry based registerServiceEntity.
     *
     * @param uuid uuid of registerServiceEntity.
     * @return Returns the database entry for the provided arguments.
     */
    RegisterServiceEntity getRegisterServiceById(UUID uuid);

    /**
     * Method to list database entry based registerServiceEntity by query model.
     *
     * @param registerServiceEntity registerServiceEntity.
     * @return Returns the database entry for the provided arguments.
     */
    RegisterServiceEntity getRegisterService(RegisterServiceEntity registerServiceEntity);

    /**
     * Method to get all stored database entries.
     *
     * @return Returns all rows from the service status database table.
     */
    List<RegisterServiceEntity> services();

    /**
     * Remove register service entity from database by uuid.
     *
     * @param uuid uuid of register service entity
     */
    void removeById(UUID uuid);

    /**
     * Remove register service entity from database by entity.
     *
     * @param registerServiceEntity register service entity
     */
    void remove(RegisterServiceEntity registerServiceEntity);

}
