/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.serviceporting.steps;

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
import org.eclipse.xpanse.modules.deployment.serviceporting.consts.ServicePortingConstants;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Processing class for checking destroy status after destroy callback. */
@Slf4j
@Component
public class ProcessDestroyResult implements Serializable, JavaDelegate {

    private final RuntimeService runtimeService;
    private final ServiceOrderManager serviceOrderManager;
    private final ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;

    /** Constructor for ProcessDestroyResult bean. */
    @Autowired
    public ProcessDestroyResult(
            RuntimeService runtimeService,
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
        UUID originalServiceId = (UUID) variables.get(ServicePortingConstants.ORIGINAL_SERVICE_ID);
        UUID servicePortingOrderId =
                (UUID) variables.get(ServicePortingConstants.SERVICE_PORTING_ORDER_ID);
        log.info(
                "Service porting workflow of instance id : {} start check destroy status.",
                processInstanceId);
        try {
            ServiceDeploymentEntity serviceDeploymentEntity =
                    serviceDeploymentEntityHandler.getServiceDeploymentEntity(originalServiceId);
            if (Objects.nonNull(serviceDeploymentEntity)
                    && serviceDeploymentEntity.getServiceDeploymentState()
                            == ServiceDeploymentState.DESTROY_SUCCESS) {
                runtimeService.setVariable(
                        processInstanceId, ServicePortingConstants.IS_DESTROY_SUCCESS, true);
                serviceOrderManager.completeOrderProgress(
                        servicePortingOrderId, TaskStatus.SUCCESSFUL, null);
            } else {
                int destroyRetryNum = getDestroyRetryNum(variables);
                runtimeService.setVariable(
                        processInstanceId, ServicePortingConstants.IS_DESTROY_SUCCESS, false);
                runtimeService.setVariable(
                        processInstanceId,
                        ServicePortingConstants.DESTROY_RETRY_NUM,
                        destroyRetryNum + 1);
                log.info(
                        "Process failed destroy task of service porting workflow with id:{}. "
                                + "RetryCount:{}",
                        processInstanceId,
                        destroyRetryNum);
                if (destroyRetryNum >= 1) {
                    String userId = (String) variables.get(ServicePortingConstants.USER_ID);
                    runtimeService.setVariable(
                            processInstanceId, ServicePortingConstants.ASSIGNEE, userId);
                    String resultMessage =
                            (String) variables.get(ServicePortingConstants.RESULT_MESSAGE);
                    serviceOrderManager.completeOrderProgress(
                            servicePortingOrderId,
                            TaskStatus.FAILED,
                            ErrorResponse.errorResponse(
                                    ErrorType.DESTROY_FAILED_EXCEPTION, List.of(resultMessage)));
                }
            }
        } catch (Exception e) {
            log.error(
                    "Failed to process destroy task of service porting workflow with id:{}",
                    processInstanceId,
                    e);
            runtimeService.setVariable(
                    processInstanceId, ServicePortingConstants.IS_DESTROY_SUCCESS, false);
            serviceOrderManager.completeOrderProgress(
                    servicePortingOrderId,
                    TaskStatus.FAILED,
                    ErrorResponse.errorResponse(
                            ErrorType.DESTROY_FAILED_EXCEPTION, List.of(e.getMessage())));
        }
    }

    private int getDestroyRetryNum(Map<String, Object> variables) {
        if (Objects.isNull(variables.get(ServicePortingConstants.DESTROY_RETRY_NUM))) {
            return 0;
        }
        return (int) variables.get(ServicePortingConstants.DESTROY_RETRY_NUM);
    }
}
