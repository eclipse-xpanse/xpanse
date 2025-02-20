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
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.springframework.stereotype.Component;

/** Bean for grouping common methods for handling ServiceDeploymentEntity. */
@Slf4j
@Component
public class ServiceDeploymentEntityHandler {

    @Resource private ServiceDeploymentStorage serviceDeploymentStorage;

    /**
     * Get deploy service entity by id.
     *
     * @param id service id.
     * @return deploy service entity.
     */
    public ServiceDeploymentEntity getServiceDeploymentEntity(UUID id) {
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
     * Store and flush service deployment entity.
     *
     * @param serviceDeploymentEntity service deployment entity.
     * @return updated service deployment entity.
     */
    public ServiceDeploymentEntity storeAndFlush(ServiceDeploymentEntity serviceDeploymentEntity) {
        return serviceDeploymentStorage.storeAndFlush(serviceDeploymentEntity);
    }

    /**
     * Update service deployment status.
     *
     * @param serviceDeployment service deployment entity.
     * @param state deployment status
     */
    public void updateServiceDeploymentStatus(
            ServiceDeploymentEntity serviceDeployment, ServiceDeploymentState state) {
        serviceDeployment.setServiceDeploymentState(state);
        serviceDeploymentStorage.storeAndFlush(serviceDeployment);
    }
}
