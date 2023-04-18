/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import org.eclipse.xpanse.modules.deployment.deployers.terraform.DeployTask;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfValidationResult;
import org.eclipse.xpanse.modules.models.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.resource.Ocl;
import org.eclipse.xpanse.modules.models.service.DeployResult;

/**
 * Interface to produce a service.
 */
public interface Deployment {

    DeployResult deploy(DeployTask task);

    DeployResult destroy(DeployTask task, String tfState);

    DeployerKind getDeployerKind();

    TfValidationResult validate(Ocl ocl);
}
