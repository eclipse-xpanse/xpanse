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
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.springframework.stereotype.Component;

/**
 * Bean for grouping common methods for handling DeployServiceEntity entities.
 */
@Slf4j
@Component
public class DeployServiceEntityHandler {

    @Resource
    private DeployServiceStorage deployServiceStorage;

    /**
     * Get deploy service entity by id.
     *
     * @param id task id.
     * @return deploy service entity.
     */
    public DeployServiceEntity getDeployServiceEntity(UUID id) {
        DeployServiceEntity deployServiceEntity = deployServiceStorage.findDeployServiceById(id);
        if (Objects.isNull(deployServiceEntity)) {
            String errorMsg = String.format("Service with id %s not found.", id);
            log.error(errorMsg);
            throw new ServiceNotDeployedException(errorMsg);
        }
        return deployServiceEntity;
    }

    public DeployServiceEntity storeAndFlush(DeployServiceEntity deployServiceEntity) {
        return deployServiceStorage.storeAndFlush(deployServiceEntity);
    }


}
