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

/** Processing class for checking deployment status after deployment callback. */
@Slf4j
@Component
public class ProcessDeploymentResult implements Serializable, JavaDelegate {

    private final RuntimeService runtimeService;
    private final ServiceOrderManager serviceOrderManager;
    private final ServiceDeploymentEntityHandler serviceDeploymentEntityHandler;

    /** Constructor for ProcessDeploymentResult bean. */
    @Autowired
    public ProcessDeploymentResult(
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
        UUID servicePortingOrderId =
                (UUID) variables.get(ServicePortingConstants.SERVICE_PORTING_ORDER_ID);
        try {
            UUID newServiceId = (UUID) variables.get(ServicePortingConstants.NEW_SERVICE_ID);
            log.info(
                    "Service porting workflow of instance id : {} check deploy service status with"
                            + " id:{}",
                    processInstanceId,
                    newServiceId.toString());
            ServiceDeploymentEntity serviceDeploymentEntity =
                    serviceDeploymentEntityHandler.getServiceDeploymentEntity(newServiceId);
            if (Objects.nonNull(serviceDeploymentEntity)
                    && serviceDeploymentEntity.getServiceDeploymentState()
                            == ServiceDeploymentState.DEPLOY_SUCCESS) {
                runtimeService.setVariable(
                        processInstanceId, ServicePortingConstants.IS_DEPLOY_SUCCESS, true);
            } else {
                int deployRetryNum = getDeployRetryNum(variables);
                runtimeService.setVariable(
                        processInstanceId,
                        ServicePortingConstants.DEPLOY_RETRY_NUM,
                        deployRetryNum + 1);
                runtimeService.setVariable(
                        processInstanceId, ServicePortingConstants.IS_DEPLOY_SUCCESS, false);
                log.info(
                        "Process failed deployment task of service porting workflow with id:{}. "
                                + "RetryCount:{}",
                        processInstanceId,
                        deployRetryNum);
                if (deployRetryNum >= 1) {
                    String userId = (String) variables.get(ServicePortingConstants.USER_ID);
                    runtimeService.setVariable(
                            processInstanceId, ServicePortingConstants.ASSIGNEE, userId);

                    String resultMessage =
                            (String) variables.get(ServicePortingConstants.RESULT_MESSAGE);
                    serviceOrderManager.completeOrderProgress(
                            servicePortingOrderId,
                            TaskStatus.FAILED,
                            ErrorResponse.errorResponse(
                                    ErrorType.DEPLOYMENT_FAILED_EXCEPTION, List.of(resultMessage)));
                }
            }
        } catch (Exception e) {
            log.error(
                    "Failed to process deployment task of service porting workflow with id:{}",
                    processInstanceId,
                    e);
            runtimeService.setVariable(
                    processInstanceId, ServicePortingConstants.IS_DEPLOY_SUCCESS, false);
            serviceOrderManager.completeOrderProgress(
                    servicePortingOrderId,
                    TaskStatus.FAILED,
                    ErrorResponse.errorResponse(
                            ErrorType.DEPLOYMENT_FAILED_EXCEPTION, List.of(e.getMessage())));
        }
    }

    private int getDeployRetryNum(Map<String, Object> variables) {
        if (Objects.isNull(variables.get(ServicePortingConstants.DEPLOY_RETRY_NUM))) {
            return 0;
        }
        return (int) variables.get(ServicePortingConstants.DEPLOY_RETRY_NUM);
    }
}
