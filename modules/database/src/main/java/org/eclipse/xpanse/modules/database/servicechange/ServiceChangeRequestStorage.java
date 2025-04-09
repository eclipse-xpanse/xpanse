/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicechange;

import java.util.List;
import java.util.UUID;

/** Interface for persist of serviceChangeRequestEntity. */
public interface ServiceChangeRequestStorage {

    /**
     * Add or update service configuration data to database.
     *
     * @param serviceChangeRequestEntity serviceChangeRequestEntity.
     */
    ServiceChangeRequestEntity storeAndFlush(ServiceChangeRequestEntity serviceChangeRequestEntity);

    /** Batch add or update service change details data to database. */
    <S extends ServiceChangeRequestEntity> void saveAll(Iterable<S> entities);

    /** Method to list database entry based ServiceChangeRequestEntity. */
    List<ServiceChangeRequestEntity> getServiceChangeRequestEntities(
            ServiceChangeRequestQueryModel query);

    /**
     * Query ServiceChangeRequestEntity by id.
     *
     * @param changeId id of the update request.
     * @return serviceChangeRequestEntity.
     */
    ServiceChangeRequestEntity findById(UUID changeId);
}
