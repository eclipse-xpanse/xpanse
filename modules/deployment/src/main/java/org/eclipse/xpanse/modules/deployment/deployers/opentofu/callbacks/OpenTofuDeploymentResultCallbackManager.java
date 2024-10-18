/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.callbacks;

import jakarta.annotation.Resource;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.DeployResultManager;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuResult;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.TfResourceTransUtils;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.springframework.stereotype.Component;

/**
 * Bean for managing deployment and destroy callback functions.
 */
@Slf4j
@Component
public class OpenTofuDeploymentResultCallbackManager {
    @Resource
    private DeployResultManager deployResultManager;

    /**
     * Handle the callback of the order task.
     *
     * @param orderId the orderId of the task.
     * @param result  execution result of the task.
     */
    public void orderCallback(UUID orderId, OpenTofuResult result) {
        DeployResult deployResult = getDeployResult(result);
        deployResult.setOrderId(orderId);
        deployResultManager.updateServiceWithDeployResult(deployResult);
    }


    private DeployResult getDeployResult(OpenTofuResult result) {
        DeployResult deployResult = new DeployResult();
        deployResult.setDeployerVersionUsed(result.getOpenTofuVersionUsed());
        if (Boolean.FALSE.equals(result.getCommandSuccessful())) {
            deployResult.setIsTaskSuccessful(false);
            deployResult.setMessage(result.getCommandStdError());
        } else {
            deployResult.setIsTaskSuccessful(true);
            deployResult.setMessage(null);
        }
        deployResult.setTfStateContent(result.getTerraformState());
        deployResult.getPrivateProperties()
                .put(TfResourceTransUtils.STATE_FILE_NAME, result.getTerraformState());
        if (Objects.nonNull(result.getImportantFileContentMap())) {
            deployResult.getPrivateProperties().putAll(result.getImportantFileContentMap());
        }
        return deployResult;
    }
}
