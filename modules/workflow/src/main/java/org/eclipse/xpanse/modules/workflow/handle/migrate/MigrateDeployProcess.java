/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.workflow.handle.migrate;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.MigrateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.workflow.consts.MigrateConstants;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Migration process deployment service processing class.
 */
@Slf4j
@Component
public class MigrateDeployProcess implements Serializable, JavaDelegate {

    private static RuntimeService runtimeService;
    private static DeployService deployService;

    @Autowired
    public void setRuntimeService(RuntimeService runtimeService) {
        MigrateDeployProcess.runtimeService = runtimeService;
    }

    @Autowired
    public void setDeployService(DeployService deployService) {
        MigrateDeployProcess.deployService = deployService;
    }

    /**
     * Methods when performing deployment tasks.
     */
    @SneakyThrows
    @Override
    public void execute(DelegateExecution execution) {

        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        UUID newId = (UUID) variables.get(MigrateConstants.NEW_ID);
        runtimeService.updateBusinessKey(processInstanceId, newId.toString());
        int deployRetryNum = (int) variables.get(MigrateConstants.DEPLOY_RETRY_NUM);

        runtimeService.setVariable(processInstanceId, MigrateConstants.DEPLOY_RETRY_NUM,
                deployRetryNum + 1);
        if (deployRetryNum > 0) {
            log.info("Process instance: {} retry deployment service : {},RetryNum:{}",
                    processInstanceId, newId, deployRetryNum);
        }
        MigrateRequest migrateRequest =
                (MigrateRequest) variables.get(MigrateConstants.MIGRATE_REQUEST);
        DeployRequest deployRequest = new DeployRequest();
        BeanUtils.copyProperties(migrateRequest, deployRequest);
        String userId = (String) variables.get(MigrateConstants.USER_ID);

        CompletableFuture<DeployServiceEntity> future =
                deployService.deployService(newId, userId, deployRequest);
        DeployServiceEntity result = future.get();

        boolean deploySuccess = Objects.nonNull(result) &&
                ServiceDeploymentState.DEPLOY_SUCCESS == result.getServiceDeploymentState();
        if (!deploySuccess && deployRetryNum >= 1) {
            runtimeService.setVariable(processInstanceId, MigrateConstants.ASSIGNEE, userId);
        }
        runtimeService.setVariable(processInstanceId, MigrateConstants.IS_DEPLOY_SUCCESS,
                deploySuccess);
    }
}
