/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicetemplate;

import java.util.List;
import java.util.UUID;

/**
 * Interface for persist of ServiceTemplate.
 */
public interface ServiceTemplateStorage {

    /**
     * Add or update managed service data to database.
     *
     * @param serviceTemplateEntity the model of registered service.
     */
    ServiceTemplateEntity storeAndFlush(ServiceTemplateEntity serviceTemplateEntity);

    /**
     * Method to list database entry based ServiceTemplateEntity.
     *
     * @param query query model for search service template entity.
     * @return Returns the database entry for the provided arguments.
     */
    List<ServiceTemplateEntity> listServiceTemplates(ServiceTemplateQueryModel query);

    /**
     * Method to get database entry based ServiceTemplateEntity.
     *
     * @param uuid uuid of ServiceTemplateEntity.
     * @return Returns the database entry for the provided arguments.
     */
    ServiceTemplateEntity getServiceTemplateById(UUID uuid);

    /**
     * Method to list database entry based ServiceTemplateEntity by query model.
     *
     * @param serviceTemplateEntity the model of registered service.
     * @return Returns the database entry for the provided arguments.
     */
    ServiceTemplateEntity findServiceTemplate(ServiceTemplateEntity serviceTemplateEntity);

    /**
     * Remove service template entity from database by uuid.
     *
     * @param uuid uuid of service template entity
     */
    void removeById(UUID uuid);

    /**
     * Remove service template entity from database by entity.
     *
     * @param serviceTemplateEntity service template entity
     */
    void remove(ServiceTemplateEntity serviceTemplateEntity);

}
