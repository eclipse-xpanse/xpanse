/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicetemplatehistory;

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

}
