/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.recreate.steps;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.eclipse.xpanse.modules.database.servicerecreate.ServiceRecreateEntity;
import org.eclipse.xpanse.modules.deployment.ServiceDeploymentEntityHandler;
import org.eclipse.xpanse.modules.deployment.recreate.RecreateService;
import org.eclipse.xpanse.modules.deployment.recreate.consts.RecreateConstants;
import org.eclipse.xpanse.modules.models.workflow.recreate.enums.RecreateStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Processing class for checking deployment status after deployment callback.
 */
@Slf4j
@Component
public class ProcessRecreateDeploymentResult implements Serializable, JavaDelegate {

    private final RuntimeService runtimeService;
    private final RecreateService recreateService;
    private final ServiceDeploymentEntityHandler deploymentEntityHandler;

    /**
     * Constructor for ProcessDeploymentResult bean.
     */
    @Autowired
    public ProcessRecreateDeploymentResult(RuntimeService runtimeService,
                                           RecreateService recreateService,
                                           ServiceDeploymentEntityHandler deploymentEntityHandler) {
        this.runtimeService = runtimeService;
        this.recreateService = recreateService;
        this.deploymentEntityHandler = deploymentEntityHandler;
    }

    @Override
    public void execute(DelegateExecution execution) {

        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        UUID originalServiceId = (UUID) variables.get(RecreateConstants.ID);
        log.info("Recreate workflow of Instance Id : {} check deploy service status with id:{}",
                processInstanceId, originalServiceId.toString());

        ServiceRecreateEntity serviceRecreateEntity =
                recreateService.getServiceRecreateEntityById(UUID.fromString(processInstanceId));
        boolean isDeploySuccess =
                deploymentEntityHandler.isServiceDeployedSuccess(originalServiceId);
        if (isDeploySuccess) {
            recreateService.updateServiceRecreateStatus(serviceRecreateEntity,
                    RecreateStatus.DEPLOY_COMPLETED, OffsetDateTime.now());
            runtimeService.setVariable(processInstanceId, RecreateConstants.IS_DEPLOY_SUCCESS,
                    true);
            log.info("deployment step completed for recreate order {}", processInstanceId);
        } else {
            recreateService.updateServiceRecreateStatus(serviceRecreateEntity,
                    RecreateStatus.DEPLOY_FAILED, OffsetDateTime.now());
            runtimeService.setVariable(processInstanceId, RecreateConstants.IS_DEPLOY_SUCCESS,
                    false);
            int deployRetryNum = updateDeployRetryNum(processInstanceId, variables);
            if (deployRetryNum >= 1) {
                String userId = (String) variables.get(RecreateConstants.USER_ID);
                runtimeService.setVariable(processInstanceId, RecreateConstants.ASSIGNEE,
                        userId);
            }
        }
    }

    private int updateDeployRetryNum(String processInstanceId, Map<String, Object> variables) {
        int deployRetryNum = (int) variables.get(RecreateConstants.DEPLOY_RETRY_NUM);
        if (deployRetryNum > 0) {
            log.info("Process instance: {} retry deployment service, RetryCount:{}",
                    processInstanceId, deployRetryNum);
        }
        runtimeService.setVariable(processInstanceId, RecreateConstants.DEPLOY_RETRY_NUM,
                deployRetryNum + 1);
        return deployRetryNum;
    }
}
