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

/** Processing class for checking destroy status after destroy callback. */
@Slf4j
@Component
public class ProcessRecreateDestroyResult implements Serializable, JavaDelegate {

    private final RuntimeService runtimeService;
    private final ServiceOrderManager serviceOrderManager;
    private final ServiceDeploymentEntityHandler deploymentEntityHandler;

    /** Constructor for ProcessDestroyResult bean. */
    @Autowired
    public ProcessRecreateDestroyResult(
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
                "Recreate workflow of instance id : {} start check destroy status.",
                processInstanceId);

        try {
            ServiceDeploymentEntity serviceDeploymentEntity =
                    deploymentEntityHandler.getServiceDeploymentEntity(serviceId);

            if (Objects.nonNull(serviceDeploymentEntity)
                    && serviceDeploymentEntity.getServiceDeploymentState()
                            == ServiceDeploymentState.DESTROY_SUCCESS) {
                runtimeService.setVariable(
                        processInstanceId, RecreateConstants.IS_DESTROY_SUCCESS, true);
                runtimeService.setVariable(
                        processInstanceId, RecreateConstants.DEPLOY_RETRY_NUM, 0);
                log.info(
                        "Destroy step completed for recreate order workflow with id {}",
                        processInstanceId);
            } else {
                runtimeService.setVariable(
                        processInstanceId, RecreateConstants.IS_DESTROY_SUCCESS, false);
                int destroyRetryNum = getDestroyRetryNum(variables);
                runtimeService.setVariable(
                        processInstanceId,
                        RecreateConstants.DESTROY_RETRY_NUM,
                        destroyRetryNum + 1);
                log.info(
                        "Process instance： {} retry to destroy task during recreation workflow. "
                                + "RetryCount:{}",
                        processInstanceId,
                        destroyRetryNum);
                if (destroyRetryNum >= 1) {
                    String userId = (String) variables.get(RecreateConstants.USER_ID);
                    runtimeService.setVariable(
                            processInstanceId, RecreateConstants.ASSIGNEE, userId);
                    String resultMessage = (String) variables.get(RecreateConstants.RESULT_MESSAGE);
                    serviceOrderManager.completeOrderProgress(
                            recreateOrderId,
                            TaskStatus.FAILED,
                            ErrorResponse.errorResponse(
                                    ErrorType.DESTROY_FAILED_EXCEPTION, List.of(resultMessage)));
                }
            }
        } catch (Exception e) {
            log.error(
                    "Failed to process destroy task of recreation workflow with id:{}",
                    processInstanceId,
                    e);
            runtimeService.setVariable(
                    processInstanceId, RecreateConstants.IS_DESTROY_SUCCESS, false);
            serviceOrderManager.completeOrderProgress(
                    recreateOrderId,
                    TaskStatus.FAILED,
                    ErrorResponse.errorResponse(
                            ErrorType.DESTROY_FAILED_EXCEPTION, List.of(e.getMessage())));
        }
    }

    private int getDestroyRetryNum(Map<String, Object> variables) {
        if (Objects.isNull(variables.get(RecreateConstants.DESTROY_RETRY_NUM))) {
            return 0;
        }
        return (int) variables.get(RecreateConstants.DESTROY_RETRY_NUM);
    }
}
