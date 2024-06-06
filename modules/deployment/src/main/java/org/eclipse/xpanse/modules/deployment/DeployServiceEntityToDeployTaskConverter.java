/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.stereotype.Component;

/**
 * Bean to create DeployTask object from DeployServiceEntity. This is needed for everything
 * other than deploy tasks.
 */
@Component
public class DeployServiceEntityToDeployTaskConverter {

    @Resource
    private ServiceTemplateStorage serviceTemplateStorage;

    /**
     * Method to create a DeployTask from DeployServiceEntity.
     *
     * @param deployServiceEntity DeployServiceEntity object.
     * @return DeployTask object.
     */
    public DeployTask getDeployTaskByStoredService(DeployServiceEntity deployServiceEntity) {
        // Set Ocl and CreateRequest
        DeployTask deployTask = new DeployTask();
        deployTask.setId(deployServiceEntity.getId());
        deployTask.setDeployRequest(deployServiceEntity.getDeployRequest());
        ServiceTemplateEntity serviceTemplateEntity = serviceTemplateStorage.getServiceTemplateById(
                deployServiceEntity.getServiceTemplateId());
        deployTask.setOcl(serviceTemplateEntity.getOcl());
        deployTask.setServiceTemplateId(serviceTemplateEntity.getId());
        return deployTask;

    }
}
