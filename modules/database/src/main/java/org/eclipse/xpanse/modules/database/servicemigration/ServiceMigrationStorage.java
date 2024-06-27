/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicemigration;

import java.util.List;
import java.util.UUID;

/**
 * Interface for persist of ServiceMigrationEntity.
 */
public interface ServiceMigrationStorage {

    /**
     * Add or update service migration data to database.
     *
     * @param serviceMigrationEntity the entity of service migration.
     * @return serviceMigrationEntity the entity of service migration.
     */
    ServiceMigrationEntity storeAndFlush(ServiceMigrationEntity serviceMigrationEntity);

    /**
     * Method to get stored database entries by query model.
     *
     * @param query query model.
     * @return Returns all rows matched the query info from the database table.
     */
    List<ServiceMigrationEntity> listServiceMigrations(ServiceMigrationQueryModel query);

    /**
     * Get ServiceMigrationEntity using ID.
     *
     * @param id the id of the ServiceMigrationEntity.
     * @return serviceMigrationEntity.
     */
    ServiceMigrationEntity findServiceMigrationById(UUID id);
}
