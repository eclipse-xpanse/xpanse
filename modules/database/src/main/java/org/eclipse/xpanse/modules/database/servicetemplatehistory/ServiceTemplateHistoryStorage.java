/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicetemplatehistory;

import java.util.List;
import java.util.UUID;

/**
 * Interface for persist of ServiceTemplateHistory.
 */
public interface ServiceTemplateHistoryStorage {


    /**
     * Store and flush serviceTemplateHistoryEntity.
     *
     * @param serviceTemplateHistoryEntity to be stored.
     * @return updated serviceTemplateHistoryEntity.
     */
    ServiceTemplateHistoryEntity storeAndFlush(
            ServiceTemplateHistoryEntity serviceTemplateHistoryEntity);

    /**
     * Get ServiceTemplateHistoryEntity by changeId.
     *
     * @param changeId given changeId
     * @return ServiceTemplateHistoryEntity
     */
    ServiceTemplateHistoryEntity getEntityById(UUID changeId);


    /**
     * List service template history by query model
     *
     * @param queryModel query model
     * @return list of service template history
     */
    List<ServiceTemplateHistoryEntity> listServiceTemplateHistoryByQueryModel(
            ServiceTemplateHistoryQueryModel queryModel);

}
