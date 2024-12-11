/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.resource;

import java.util.UUID;

/** Interface for persist of ServiceResource. */
public interface ServiceResourceStorage {

    void deleteByDeployServiceId(UUID id);

    /**
     * Get detail of service resource using ID.
     *
     * @param id the ID of deployed resource.
     * @return DeployResourceEntity
     */
    ServiceResourceEntity findServiceResourceById(UUID id);

    /**
     * Get detail of service resource using ID.
     *
     * @param resourceId the RESOURCE_ID of deployed resource.
     * @return DeployResourceEntity
     */
    ServiceResourceEntity findServiceResourceByResourceId(String resourceId);
}
