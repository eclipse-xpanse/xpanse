/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration.update;

import java.util.List;
import java.util.UUID;

/**
 * Interface for persist of ServiceConfigurationUpdateRequest.
 */
public interface ServiceConfigurationUpdateStorage {

    /**
     * Add or update service configuration data to database.
     *
     * @param serviceConfigurationUpdateRequest serviceConfigurationUpdateRequest.
     */
    ServiceConfigurationUpdateRequest storeAndFlush(
            ServiceConfigurationUpdateRequest serviceConfigurationUpdateRequest);

    /**
     * Batch add or update service configuration data to database.
     */
    <S extends ServiceConfigurationUpdateRequest> List<S> saveAll(Iterable<S> entities);

    /**
     * Method to list database entry based ServiceConfigurationUpdateRequest.
     */
    List<ServiceConfigurationUpdateRequest> listServiceConfigurationUpdateRequests(
            ServiceConfigurationUpdateRequestQueryModel query);

    /**
     * Query ServiceConfigurationUpdateRequest by id.
     *
     * @param changeId id of the update request.
     * @return serviceConfigurationUpdateRequest.
     */
    ServiceConfigurationUpdateRequest findById(UUID changeId);
}
