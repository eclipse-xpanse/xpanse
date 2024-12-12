/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.junit.jupiter.api.Test;

public class DeploymentStatusUpdateTest {

    @Test
    void deploymentTestGetters() {
        DeploymentStatusUpdate deploymentStatusUpdate =
                new DeploymentStatusUpdate(ServiceDeploymentState.DEPLOY_SUCCESS, true);
        assertTrue(deploymentStatusUpdate.getIsOrderCompleted());
        assertEquals(
                deploymentStatusUpdate.getServiceDeploymentState(),
                ServiceDeploymentState.DEPLOY_SUCCESS);
    }
}
