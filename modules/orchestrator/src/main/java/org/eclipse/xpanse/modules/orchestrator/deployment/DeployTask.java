/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.deployment;

import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicy;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;

/**
 * The task for the deployment.
 */
@Data
public class DeployTask {

    /**
     * The id of the deployment task.
     */
    private UUID id;

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
     * The specific xpanse resource handler for the csp.
     */
    private DeployResourceHandler deployResourceHandler;

    /**
     * The service policies belongs to the registered service template.
     */
    private List<ServicePolicy> servicePolicies;

}
