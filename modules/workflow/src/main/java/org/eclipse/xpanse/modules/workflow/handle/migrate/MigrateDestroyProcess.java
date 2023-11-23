/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.workflow.handle.migrate;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.workflow.consts.MigrateConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Migration process destroy service processing class.
 */
@Slf4j
@Component
public class MigrateDestroyProcess implements Serializable, JavaDelegate {

    private final RuntimeService runtimeService;
    private final DeployService deployService;

    @Autowired
    public MigrateDestroyProcess(RuntimeService runtimeService,
                                 DeployService deployService) {
        this.runtimeService = runtimeService;
        this.deployService = deployService;
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
        int destroyRetryNum = (int) variables.get(MigrateConstants.DESTROY_RETRY_NUM);
        if (destroyRetryNum > 0) {
            log.info("Process instance: {} retry destroy service : {},RetryNum:{}",
                    processInstanceId, id, destroyRetryNum);
        }
        runtimeService.setVariable(processInstanceId, MigrateConstants.DESTROY_RETRY_NUM,
                destroyRetryNum + 1);
        CompletableFuture<DeployServiceEntity> future = deployService.destroyService(id);
        DeployServiceEntity deployServiceEntity = future.get();

        boolean destroySuccess = Objects.nonNull(deployServiceEntity)
                && ServiceDeploymentState.DEPLOY_SUCCESS
                == deployServiceEntity.getServiceDeploymentState();
        runtimeService.setVariable(processInstanceId, MigrateConstants.IS_DESTROY_SUCCESS,
                destroySuccess);
    }
}
