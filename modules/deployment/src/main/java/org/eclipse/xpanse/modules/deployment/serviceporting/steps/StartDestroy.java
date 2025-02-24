/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.serviceporting.steps;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.deployment.ServiceOrderManager;
import org.eclipse.xpanse.modules.deployment.serviceporting.consts.ServicePortingConstants;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Service porting process destroy service processing class. */
@Slf4j
@Component
public class StartDestroy implements Serializable, JavaDelegate {

    private final DeployService deployService;
    private final RuntimeService runtimeService;
    private final ServiceOrderManager serviceOrderManager;

    /** Constructor for StartDestroy bean. */
    @Autowired
    public StartDestroy(
            DeployService deployService,
            RuntimeService runtimeService,
            ServiceOrderManager serviceOrderManager) {
        this.deployService = deployService;
        this.runtimeService = runtimeService;
        this.serviceOrderManager = serviceOrderManager;
    }

    /** Methods when performing destroy tasks. */
    @SneakyThrows
    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        DeployTask destroyTask = getDestroyTaskForPorting(processInstanceId, variables);
        int retryTimes = (int) variables.get(ServicePortingConstants.DESTROY_RETRY_NUM);
        log.info(
                "Start destroy task in service porting workflow with id:{}. Task:{}. Retry"
                        + " times:{}",
                processInstanceId,
                destroyTask,
                retryTimes);
        try {
            ServiceOrder serviceOrder = deployService.destroyServiceByWorkflow(destroyTask);
            log.info("Started destroy task with order: {} successfully.", serviceOrder);
        } catch (Exception e) {
            log.error(
                    "Started destroy task in service porting workflow with id:{} failed.",
                    processInstanceId,
                    e);
            runtimeService.setVariable(
                    processInstanceId, ServicePortingConstants.IS_DEPLOY_SUCCESS, false);
            serviceOrderManager.completeOrderProgress(
                    destroyTask.getParentOrderId(),
                    TaskStatus.FAILED,
                    ErrorResponse.errorResponse(
                            ErrorType.DESTROY_FAILED_EXCEPTION, List.of(e.getMessage())));
        }
    }

    private DeployTask getDestroyTaskForPorting(
            String processInstanceId, Map<String, Object> variables) {
        DeployTask destroyTask = new DeployTask();
        destroyTask.setWorkflowId(processInstanceId);
        UUID originalServiceId = (UUID) variables.get(ServicePortingConstants.ORIGINAL_SERVICE_ID);
        destroyTask.setOriginalServiceId(originalServiceId);
        destroyTask.setServiceId(originalServiceId);
        UUID servicePortingOrderId =
                (UUID) variables.get(ServicePortingConstants.SERVICE_PORTING_ORDER_ID);
        destroyTask.setParentOrderId(servicePortingOrderId);
        String userId = (String) variables.get(ServicePortingConstants.USER_ID);
        destroyTask.setUserId(userId);
        return destroyTask;
    }
}
