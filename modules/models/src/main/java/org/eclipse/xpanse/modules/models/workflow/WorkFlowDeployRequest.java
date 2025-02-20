/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.workflow;

import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;

/** deployment request model for workflow tasks. */
@Data
public class WorkFlowDeployRequest {

    /** The id of the original service to change. */
    private UUID originalServiceId;

    /** The id of the new service to deploy. */
    private UUID newServiceId;

    /** The id of the parent order. */
    private UUID parentOrderId;

    /** The id of the workflow instance. */
    private String workflowId;

    /** The id of the user created the task. */
    private String userId;

    /** The deployment request for the workflow task. */
    private DeployRequest deployRequest;
}
