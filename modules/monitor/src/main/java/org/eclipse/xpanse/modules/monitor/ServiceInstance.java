/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor;

import java.util.List;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.service.DeployResource;

/**
 * The service instance.
 */
public class ServiceInstance {

    /**
     * The service entity.
     */
    private final DeployServiceEntity serviceEntity;

    /**
     * The deployed resources of the service instance.
     */
    private List<DeployResource> deployResources;

    /**
     * The constructor.
     */
    public ServiceInstance(DeployServiceEntity deployServiceEntity) {
        this.serviceEntity = deployServiceEntity;
    }

    /**
     * Get the resources list of the service instance.
     */
    public List<DeployResource> getDeployResources() {
        return null;
    }
}
