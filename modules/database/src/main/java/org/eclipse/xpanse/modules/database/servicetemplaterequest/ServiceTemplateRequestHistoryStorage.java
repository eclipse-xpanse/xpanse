/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicetemplaterequest;

import java.util.List;
import java.util.UUID;

/** Interface for persist of ServiceTemplateRequestHistory. */
public interface ServiceTemplateRequestHistoryStorage {

    /**
     * Store and flush serviceTemplateRequestHistoryEntity.
     *
     * @param serviceTemplateRequestHistoryEntity to be stored.
     * @return updated serviceTemplateRequestHistoryEntity.
     */
    ServiceTemplateRequestHistoryEntity storeAndFlush(
            ServiceTemplateRequestHistoryEntity serviceTemplateRequestHistoryEntity);

    /**
     * Get ServiceTemplateRequestHistoryEntity by request id.
     *
     * @param requestId given request id.
     * @return ServiceTemplateRequestHistoryEntity
     */
    ServiceTemplateRequestHistoryEntity getEntityByRequestId(UUID requestId);

    /**
     * List service template request history by query model.
     *
     * @param queryModel query model
     * @return list of service template history
     */
    List<ServiceTemplateRequestHistoryEntity> listServiceTemplateRequestHistoryByQueryModel(
            ServiceTemplateRequestHistoryQueryModel queryModel);

    /** cancel requests in batch. */
    void cancelRequestsInBatch(List<ServiceTemplateRequestHistoryEntity> requests);
}
