/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.migration.steps;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.eclipse.xpanse.modules.deployment.ServiceDeploymentEntityHandler;
import org.eclipse.xpanse.modules.deployment.ServiceOrderManager;
import org.eclipse.xpanse.modules.deployment.migration.consts.MigrateConstants;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Processing class for checking deployment status after deployment callback.
 */
@Slf4j
@Component
public class ProcessDeploymentResult implements Serializable, JavaDelegate {

    private final RuntimeService runtimeService;
    private final ServiceOrderManager serviceOrderManager;
    private final ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;

    /**
     * Constructor for ProcessDeploymentResult bean.
     */
    @Autowired
    public ProcessDeploymentResult(RuntimeService runtimeService,
                                   ServiceOrderManager serviceOrderManager,
                                   ServiceDeploymentEntityHandler serviceDeploymentEntityHandler) {
        this.runtimeService = runtimeService;
        this.serviceOrderManager = serviceOrderManager;
        this.serviceDeploymentEntityHandler = serviceDeploymentEntityHandler;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        UUID migrateOrderId = (UUID) variables.get(MigrateConstants.MIGRATE_ORDER_ID);
        try {
            UUID newServiceId = (UUID) variables.get(MigrateConstants.NEW_SERVICE_ID);
            boolean isDeploySuccess =
                    serviceDeploymentEntityHandler.isServiceDeployedSuccess(newServiceId);
            runtimeService.setVariable(processInstanceId, MigrateConstants.IS_DEPLOY_SUCCESS,
                    isDeploySuccess);
            if (!isDeploySuccess) {
                int deployRetryNum = getDeployRetryNum(variables);
                runtimeService.setVariable(processInstanceId, MigrateConstants.DEPLOY_RETRY_NUM,
                        deployRetryNum + 1);
                log.info("Process failed deployment task of migration workflow with id:{}. "
                        + "RetryCount:{}", processInstanceId, deployRetryNum);
                if (deployRetryNum >= 1) {
                    String userId = (String) variables.get(MigrateConstants.USER_ID);
                    runtimeService.setVariable(processInstanceId, MigrateConstants.ASSIGNEE,
                            userId);

                    serviceOrderManager.completeOrderProgress(migrateOrderId, TaskStatus.FAILED,
                            null);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process deployment task of migration workflow with id:{}",
                    processInstanceId, e);
            runtimeService.setVariable(processInstanceId, MigrateConstants.IS_DEPLOY_SUCCESS,
                    false);
            serviceOrderManager.completeOrderProgress(migrateOrderId,
                    TaskStatus.FAILED, e.getMessage());
        }
    }


    private int getDeployRetryNum(Map<String, Object> variables) {
        if (Objects.isNull(variables.get(MigrateConstants.DEPLOY_RETRY_NUM))) {
            return 0;
        }
        return (int) variables.get(MigrateConstants.DEPLOY_RETRY_NUM);
    }
}
