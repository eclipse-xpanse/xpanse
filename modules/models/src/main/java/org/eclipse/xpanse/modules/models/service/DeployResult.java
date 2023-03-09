/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.xpanse.modules.models.enums.TerraformExecState;

/**
 * The result of the deployment.
 */
@Data
public class DeployResult {

    /**
     * The id of the XpanseDeployTask.
     */
    private UUID id;
    /**
     * The state of the XpanseDeployTask.
     */
    private TerraformExecState state;

    /**
     * The resources of the server.
     */
    private List<DeployResource> resources;

    /**
     * The raw resources of the server.
     */
    private Map<String, Object> rawResources = new HashMap<>();

}
