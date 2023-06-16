/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.plugin.deployment;

import java.io.IOException;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.eclipse.xpanse.modules.models.service.register.enums.DeployerKind;

/**
 * Interface to produce a service.
 */
public interface Deployment {

    DeployResult deploy(DeployTask task);

    DeployResult destroy(DeployTask task, String tfState) throws IOException;

    DeployerKind getDeployerKind();

    DeployValidationResult validate(Ocl ocl);
}
