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

    DeployResult destroy(DeployTask task, String tfState);

    void deleteTaskWorkspace(String taskId);

    DeployerKind getDeployerKind();

    DeployValidationResult validate(Ocl ocl);

    String getDeployPlanAsJson(DeployTask task);
}
