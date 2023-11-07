/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.workflow.handle.migrate;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.workflow.consts.MigrateConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Migration process destroy service processing class.
 */
@Slf4j
@Component
public class MigrateDestroyProcess implements Serializable, JavaDelegate {

    private static RuntimeService runtimeService;
    private static DeployService deployService;

    @Autowired
    public void setRuntimeService(RuntimeService runtimeService) {
        MigrateDestroyProcess.runtimeService = runtimeService;
    }

    @Autowired
    public void setDeployService(DeployService deployService) {
        MigrateDestroyProcess.deployService = deployService;
    }

    /**
     * Methods when performing destroy tasks.
     */
    @SneakyThrows
    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        String id = variables.get(MigrateConstants.ID).toString();
        String userId = (String) variables.get(MigrateConstants.USER_ID);
        int destroyRetryNum = (int) variables.get(MigrateConstants.DESTROY_RETRY_NUM);
        if (destroyRetryNum > 0) {
            log.info("Process instance: {} retry destroy service : {},RetryNum:{}",
                    processInstanceId, id, destroyRetryNum);
        }
        runtimeService.setVariable(processInstanceId, MigrateConstants.DESTROY_RETRY_NUM,
                destroyRetryNum + 1);
        CompletableFuture<Void> future = deployService.destroyService(id);
        future.join();
        CompletableFuture<Boolean> destroySuccess =
                deployService.isDestroySuccess(UUID.fromString(id));
        runtimeService.setVariable(processInstanceId, MigrateConstants.IS_DESTROY_SUCCESS,
                destroySuccess.get());
    }
}
