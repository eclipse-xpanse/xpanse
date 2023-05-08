/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.database.service.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.springframework.beans.BeanUtils;

/**
 * The service instance.
 */
@Data
public class ServiceInstance {

    /**
     * The service entity.
     */
    private final DeployServiceEntity serviceEntity;
    private Csp csp;
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
     * Get deployResources.
     *
     */
    public List<DeployResource> getDeployResources() {
        List<DeployResourceEntity> deployResourceList = serviceEntity.getDeployResourceList();
        List<DeployResource> deployResources = new ArrayList<>();
        for (DeployResourceEntity deployResourceEntity : deployResourceList) {
            DeployResource deployResource = new DeployResource();
            BeanUtils.copyProperties(deployResourceEntity, deployResource);
            deployResources.add(deployResource);
        }
        return deployResources;
    }
}
