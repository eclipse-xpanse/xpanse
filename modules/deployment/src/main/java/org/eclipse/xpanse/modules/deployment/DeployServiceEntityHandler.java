/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentStorage;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.springframework.stereotype.Component;

/**
 * Bean for grouping common methods for handling DeployServiceEntity entities.
 */
@Slf4j
@Component
public class DeployServiceEntityHandler {

    @Resource
    private ServiceDeploymentStorage serviceDeploymentStorage;

    /**
     * Get deploy service entity by id.
     *
     * @param id service id.
     * @return deploy service entity.
     */
    public ServiceDeploymentEntity getDeployServiceEntity(UUID id) {
        ServiceDeploymentEntity serviceDeploymentEntity =
                serviceDeploymentStorage.findServiceDeploymentById(id);
        if (Objects.isNull(serviceDeploymentEntity)) {
            String errorMsg = String.format("Service with id %s not found.", id);
            log.error(errorMsg);
            throw new ServiceNotDeployedException(errorMsg);
        }
        return serviceDeploymentEntity;
    }

    /**
     * Store and flush deploy service entity.
     *
     * @param serviceDeploymentEntity deploy service entity.
     * @return updated deploy service entity.
     */
    public ServiceDeploymentEntity storeAndFlush(ServiceDeploymentEntity serviceDeploymentEntity) {
        return serviceDeploymentStorage.storeAndFlush(serviceDeploymentEntity);
    }

    public ServiceDeploymentEntity updateServiceDeploymentStatus(
            ServiceDeploymentEntity deployService, ServiceDeploymentState state) {
        deployService.setServiceDeploymentState(state);
        return serviceDeploymentStorage.storeAndFlush(deployService);
    }


}
