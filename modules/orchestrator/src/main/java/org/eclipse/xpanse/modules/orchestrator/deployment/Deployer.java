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
public interface Deployer {

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

    /**
     * Method to get the changes the infrastructure changes the service deployment would do for a
     * given service. This can be the terraform plan as JSON or pulumi's preview as JSON.
     * The JSON string must be something that will be used for creating rego policy files.
     * This is generally a feature provided by the underlying infrastructure deployment tool.
     *
     * @param task DeployTask of the service deployment.
     * @return JSON representation of infrastructure changes that would be created by the
     * service deployment.
     */
    String getDeploymentPlanAsJson(DeployTask task);
}
