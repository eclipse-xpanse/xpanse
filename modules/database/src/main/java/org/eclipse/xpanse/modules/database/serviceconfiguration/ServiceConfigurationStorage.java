/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration;

import java.util.UUID;

/**
 * Interface for persist of ServiceConfigurationEntity.
 */
public interface ServiceConfigurationStorage {

    /**
     * Add or update service configuration data to database.
     *
     * @param serviceConfigurationEntity the entity of service configuration.
     * @return serviceConfigurationEntity the entity of service configuration.
     */
    ServiceConfigurationEntity storeAndFlush(ServiceConfigurationEntity serviceConfigurationEntity);

    /**
     * Get detail of  service configuration using ID.
     *
     * @param id the ID of deployed service.
     * @return serviceConfigurationEntity the entity of service configuration.
     */
    ServiceConfigurationEntity findServiceConfigurationById(UUID id);

    /**
     * purge service configuration using service configuration.
     *
     * @param serviceConfigurationEntity the entity of service configuration.
     */
    void deleteServiceConfiguration(ServiceConfigurationEntity serviceConfigurationEntity);

}
