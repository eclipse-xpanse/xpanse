/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.resource;

import java.util.UUID;

/**
 * Interface for persist of DeployResource.
 */
public interface DeployResourceStorage {

    void deleteByDeployServiceId(UUID id);

    /**
     * Get detail of deployed resource using ID.
     *
     * @param id the ID of deployed resource.
     * @return DeployResourceEntity
     */
    DeployResourceEntity findDeployResourceById(UUID id);


    /**
     * Get detail of deployed resource using ID.
     *
     * @param resourceId the RESOURCE_ID of deployed resource.
     * @return DeployResourceEntity
     */
    DeployResourceEntity findDeployResourceByResourceId(String resourceId);
}
