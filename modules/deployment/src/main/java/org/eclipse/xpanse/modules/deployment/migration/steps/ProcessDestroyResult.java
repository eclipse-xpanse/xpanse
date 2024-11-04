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
 * Processing class for checking destroy status after destroy callback.
 */
@Slf4j
@Component
public class ProcessDestroyResult implements Serializable, JavaDelegate {

    private final RuntimeService runtimeService;
    private final ServiceOrderManager serviceOrderManager;
    private final ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;


    /**
     * Constructor for ProcessDestroyResult bean.
     */
    @Autowired
    public ProcessDestroyResult(RuntimeService runtimeService,
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
        UUID originalServiceId = (UUID) variables.get(MigrateConstants.ORIGINAL_SERVICE_ID);
        UUID migrateOrderId = (UUID) variables.get(MigrateConstants.MIGRATE_ORDER_ID);

        try {
            boolean isDestroySuccess =
                    serviceDeploymentEntityHandler.isServiceDestroyedSuccess(originalServiceId);
            runtimeService.setVariable(processInstanceId, MigrateConstants.IS_DESTROY_SUCCESS,
                    isDestroySuccess);
            if (isDestroySuccess) {
                serviceOrderManager.completeOrderProgress(migrateOrderId, TaskStatus.SUCCESSFUL,
                        null);
            } else {
                int destroyRetryNum = getDestroyRetryNum(variables);
                runtimeService.setVariable(processInstanceId,
                        MigrateConstants.DESTROY_RETRY_NUM, destroyRetryNum + 1);
                log.info("Process failed destroy task of migration workflow with id:{}. "
                        + "RetryCount:{}", processInstanceId, destroyRetryNum);
                if (destroyRetryNum >= 1) {
                    String userId = (String) variables.get(MigrateConstants.USER_ID);
                    runtimeService.setVariable(processInstanceId, MigrateConstants.ASSIGNEE,
                            userId);
                    serviceOrderManager.completeOrderProgress(migrateOrderId, TaskStatus.FAILED,
                            null);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process destroy task of migration workflow with id:{}",
                    processInstanceId, e);
            runtimeService.setVariable(processInstanceId, MigrateConstants.IS_DESTROY_SUCCESS,
                    false);
            serviceOrderManager.completeOrderProgress(migrateOrderId, TaskStatus.FAILED,
                    e.getMessage());

        }
    }

    private int getDestroyRetryNum(Map<String, Object> variables) {
        if (Objects.isNull(variables.get(MigrateConstants.DESTROY_RETRY_NUM))) {
            return 0;
        }
        return (int) variables.get(MigrateConstants.DESTROY_RETRY_NUM);
    }

}