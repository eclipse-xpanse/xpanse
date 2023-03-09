/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.deployment.DeployResourceHandler;
import org.eclipse.xpanse.modules.models.resource.Ocl;
import org.eclipse.xpanse.modules.models.service.CreateRequest;

/**
 * The task for the deployment.
 */
@Data
public class DeployTask {

    /**
     * The id of the DeployTask.
     */
    private UUID id;

    /**
     * The Ocl object of the DeployTask.
     */
    private CreateRequest createRequest;

    /**
     * The Ocl object of the DeployTask.
     */
    private Ocl ocl;

    /**
     * The specific xpanse resource handler for the csp.
     */
    private DeployResourceHandler deployResourceHandler;

}
