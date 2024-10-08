/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicerecreate;

import java.util.List;
import java.util.UUID;

/**
 * Interface for persist of ServiceRecreateEntity.
 */
public interface ServiceRecreateStorage {

    /**
     * Add or update service recreate data to database.
     *
     * @param serviceRecreateEntity the entity of service recreate.
     * @return serviceRecreateEntity the entity of service recreate.
     */
    ServiceRecreateEntity storeAndFlush(ServiceRecreateEntity serviceRecreateEntity);

    /**
     * Method to get stored database entries by query model.
     *
     * @param query query model.
     * @return Returns all rows matched the query info from the database table.
     */
    List<ServiceRecreateEntity> listServiceRecreates(ServiceRecreateQueryModel query);

    /**
     * Get ServiceRecreateEntity using ID.
     *
     * @param id the id of the ServiceRecreateEntity.
     * @return serviceRecreateEntity.
     */
    ServiceRecreateEntity findServiceRecreateById(UUID id);
}
