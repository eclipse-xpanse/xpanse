/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration.update;

import java.util.List;
import java.util.UUID;

/**
 * Interface for persist of ServiceConfigurationChangeDetailsEntity.
 */
public interface ServiceConfigurationChangeDetailsStorage {

    /**
     * Add or update service configuration data to database.
     *
     * @param serviceConfigurationChangeDetailsEntity serviceConfigurationChangeDetailsEntity.
     */
    ServiceConfigurationChangeDetailsEntity storeAndFlush(
            ServiceConfigurationChangeDetailsEntity serviceConfigurationChangeDetailsEntity);

    /**
     * Batch add or update service configuration data to database.
     */
    <S extends ServiceConfigurationChangeDetailsEntity> List<S> saveAll(Iterable<S> entities);

    /**
     * Method to list database entry based ServiceConfigurationChangeDetailsEntity.
     */
    List<ServiceConfigurationChangeDetailsEntity> listServiceConfigurationChangeDetails(
            ServiceConfigurationChangeDetailsQueryModel query);

    /**
     * Query ServiceConfigurationChangeDetailsEntity by id.
     *
     * @param changeId id of the update request.
     * @return serviceConfigurationChangeDetailsEntity.
     */
    ServiceConfigurationChangeDetailsEntity findById(UUID changeId);
}
