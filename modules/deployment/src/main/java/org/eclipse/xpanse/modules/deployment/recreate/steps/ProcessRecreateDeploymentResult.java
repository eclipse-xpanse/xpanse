/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.recreate.steps;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.deployment.ServiceDeploymentEntityHandler;
import org.eclipse.xpanse.modules.deployment.ServiceOrderManager;
import org.eclipse.xpanse.modules.deployment.recreate.consts.RecreateConstants;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Processing class for checking deployment status after deployment callback. */
@Slf4j
@Component
public class ProcessRecreateDeploymentResult implements Serializable, JavaDelegate {

    private final RuntimeService runtimeService;
    private final ServiceOrderManager serviceOrderManager;
    private final ServiceDeploymentEntityHandler deploymentEntityHandler;

    /** Constructor for ProcessDeploymentResult bean. */
    @Autowired
    public ProcessRecreateDeploymentResult(
            RuntimeService runtimeService,
            ServiceOrderManager serviceOrderManager,
            ServiceDeploymentEntityHandler deploymentEntityHandler) {
        this.runtimeService = runtimeService;
        this.serviceOrderManager = serviceOrderManager;
        this.deploymentEntityHandler = deploymentEntityHandler;
    }

    @Override
    public void execute(DelegateExecution execution) {

        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        UUID serviceId = (UUID) variables.get(RecreateConstants.SERVICE_ID);
        UUID recreateOrderId = (UUID) variables.get(RecreateConstants.RECREATE_ORDER_ID);
        log.info(
                "Recreate workflow of instance id : {} check deploy service status with id:{}",
                processInstanceId,
                serviceId.toString());

        try {
            ServiceDeploymentEntity serviceDeploymentEntity =
                    deploymentEntityHandler.getServiceDeploymentEntity(serviceId);

            if (Objects.nonNull(serviceDeploymentEntity)
                    && serviceDeploymentEntity.getServiceDeploymentState()
                            == ServiceDeploymentState.DEPLOY_SUCCESS) {
                serviceOrderManager.completeOrderProgress(
                        recreateOrderId, TaskStatus.SUCCESSFUL, null);
                runtimeService.setVariable(
                        processInstanceId, RecreateConstants.IS_DEPLOY_SUCCESS, true);
                log.info(
                        "Deployment step completed for recreate order workflow with id {}.",
                        processInstanceId);
            } else {
                runtimeService.setVariable(
                        processInstanceId, RecreateConstants.IS_DEPLOY_SUCCESS, false);
                int deployRetryNum = getDeployRetryNum(variables);
                runtimeService.setVariable(
                        processInstanceId, RecreateConstants.DEPLOY_RETRY_NUM, deployRetryNum + 1);
                log.info(
                        "Process instance： {} retry to deploy task during recreation workflow. "
                                + "RetryCount:{}",
                        processInstanceId,
                        deployRetryNum);
                if (deployRetryNum >= 1) {
                    String userId = (String) variables.get(RecreateConstants.USER_ID);
                    String resultMessage = (String) variables.get(RecreateConstants.RESULT_MESSAGE);
                    runtimeService.setVariable(
                            processInstanceId, RecreateConstants.ASSIGNEE, userId);
                    serviceOrderManager.completeOrderProgress(
                            recreateOrderId,
                            TaskStatus.FAILED,
                            ErrorResponse.errorResponse(
                                    ErrorType.DEPLOYMENT_FAILED_EXCEPTION, List.of(resultMessage)));
                }
            }
        } catch (Exception e) {
            log.error(
                    "Failed to process deployment task of recreation workflow with id:{}",
                    processInstanceId,
                    e);
            runtimeService.setVariable(
                    processInstanceId, RecreateConstants.IS_DEPLOY_SUCCESS, false);
            serviceOrderManager.completeOrderProgress(
                    recreateOrderId,
                    TaskStatus.FAILED,
                    ErrorResponse.errorResponse(
                            ErrorType.DEPLOYMENT_FAILED_EXCEPTION, List.of(e.getMessage())));
        }
    }

    private int getDeployRetryNum(Map<String, Object> variables) {
        if (Objects.isNull(variables.get(RecreateConstants.DEPLOY_RETRY_NUM))) {
            return 0;
        }
        return (int) variables.get(RecreateConstants.DEPLOY_RETRY_NUM);
    }
}
