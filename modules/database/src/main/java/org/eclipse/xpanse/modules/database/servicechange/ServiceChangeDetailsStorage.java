/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicechange;

import java.util.List;
import java.util.UUID;

/** Interface for persist of ServiceChangeDetailsEntity. */
public interface ServiceChangeDetailsStorage {

    /**
     * Add or update service configuration data to database.
     *
     * @param serviceChangeDetailsEntity serviceChangeDetailsEntity.
     */
    ServiceChangeDetailsEntity storeAndFlush(ServiceChangeDetailsEntity serviceChangeDetailsEntity);

    /** Batch add or update service change details data to database. */
    <S extends ServiceChangeDetailsEntity> List<S> saveAll(Iterable<S> entities);

    /** Method to list database entry based ServiceChangeDetailsEntity. */
    List<ServiceChangeDetailsEntity> listServiceChangeDetails(ServiceChangeDetailsQueryModel query);

    /**
     * Query ServiceChangeDetailsEntity by id.
     *
     * @param changeId id of the update request.
     * @return serviceChangeDetailsEntity.
     */
    ServiceChangeDetailsEntity findById(UUID changeId);
}
