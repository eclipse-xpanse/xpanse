/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.deployment;

import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;

/**
 * Interface to produce a service.
 */
public interface Deployment {

    DeployResult deploy(DeployTask task);

    /**
     * Method to delete the service. Will destroy all cloud resources used by the service.
     *
     * @param task task to be executed.
     * @return result of the destroy task.
     */
    DeployResult destroy(DeployTask task);

    void deleteTaskWorkspace(String taskId);

    DeployerKind getDeployerKind();

    DeployValidationResult validate(Ocl ocl);

    String getDeployPlanAsJson(DeployTask task);
}
