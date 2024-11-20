/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicetemplatehistory;

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

}
