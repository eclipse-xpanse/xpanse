/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.xpanse.modules.database.service.DeployResourceEntity;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.TerraformExecState;

/**
 * The result of the deployment.
 */
public class DeployResult {

    public DeployResult(DeployTask xpanseDeployTask) {
        this.task = xpanseDeployTask;
    }

    /**
     * The state of the XpanseDeployTask.
     */
    @Setter
    @Getter
    private TerraformExecState state;

    /**
     * The XpanseDeployTask.
     */
    @Getter
    private final DeployTask task;

    /**
     * The resources of the server.
     */
    @Setter
    @Getter
    private List<DeployResourceEntity> resources;

    /**
     * The raw resources of the server.
     */
    @Setter
    @Getter
    private Map<String, Object> rawResources = new HashMap<>();

}
