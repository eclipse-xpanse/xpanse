/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.deployment;

import java.io.IOException;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;

/**
 * Interface to produce a service.
 */
public interface Deployment {

    DeployResult deploy(DeployTask task);

    DeployResult destroy(DeployTask task, String tfState) throws IOException;

    DeployerKind getDeployerKind();

    DeployValidationResult validate(Ocl ocl);
}
