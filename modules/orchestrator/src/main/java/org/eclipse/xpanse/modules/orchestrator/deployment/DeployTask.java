/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.deployment;

import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;

/**
 * The task for the deployment.
 */
@Data
public class DeployTask {

    /**
     * The id of the order task.
     */
    private UUID orderId;

    /**
     * The type of the order task.
     */
    private ServiceOrderType taskType;

    /**
     * The id of the user who created the order task.
     */
    private String userId;

    /**
     * The id of the service.
     */
    private UUID serviceId;

    /**
     * Namespace of the user who registered service template.
     */
    private String namespace;

    /**
     * The Ocl object of the deployment task.
     */
    private DeployRequest deployRequest;

    /**
     * The Ocl object of the deployment task.
     */
    private Ocl ocl;

    /**
     * The id of the service template.
     */
    private UUID serviceTemplateId;

    /**
     * The scenario of the deployment task.
     */
    private DeploymentScenario deploymentScenario;

}
