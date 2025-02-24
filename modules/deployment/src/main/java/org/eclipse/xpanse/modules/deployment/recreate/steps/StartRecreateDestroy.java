/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.recreate.steps;

import java.io.Serial;
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
import org.eclipse.xpanse.modules.deployment.recreate.consts.RecreateConstants;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.eclipse.xpanse.modules.models.service.enums.OrderStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Recreate process deployment service processing class. */
@Slf4j
@Component
public class StartRecreateDestroy implements Serializable, JavaDelegate {

    @Serial private static final long serialVersionUID = 2725212494105579585L;

    private final DeployService deployService;
    private final ServiceOrderManager serviceOrderManager;
    private final RuntimeService runtimeService;

    /** Constructor for StartRecreateDeploy bean. */
    @Autowired
    public StartRecreateDestroy(
            DeployService deployService,
            ServiceOrderManager serviceOrderManager,
            RuntimeService runtimeService) {
        this.deployService = deployService;
        this.serviceOrderManager = serviceOrderManager;
        this.runtimeService = runtimeService;
    }

    /** Methods when performing deployment tasks. */
    @SneakyThrows
    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        DeployTask destroyTask = getDestroyTaskForRecreate(processInstanceId);
        int retryTimes = (int) execution.getVariable(RecreateConstants.DEPLOY_RETRY_NUM);
        log.info(
                "Start destroy task in service recreate workflow with id:{}. Task:{}. Retry"
                        + " times:{}",
                processInstanceId,
                destroyTask,
                retryTimes);
        try {
            ServiceOrder serviceOrder = deployService.destroyServiceByWorkflow(destroyTask);
            log.info("Started destroy task with order: {} successfully.", serviceOrder);
        } catch (Exception e) {
            log.error(
                    "Started destroy task in service recreate workflow with id:{} failed.",
                    processInstanceId,
                    e);
            runtimeService.setVariable(
                    processInstanceId, RecreateConstants.IS_DESTROY_SUCCESS, false);
            serviceOrderManager.completeOrderProgress(
                    destroyTask.getParentOrderId(),
                    OrderStatus.FAILED,
                    ErrorResponse.errorResponse(
                            ErrorType.DESTROY_FAILED_EXCEPTION, List.of(e.getMessage())));
        }
    }

    private DeployTask getDestroyTaskForRecreate(String processInstanceId) {
        DeployTask destroyTask = new DeployTask();
        destroyTask.setWorkflowId(processInstanceId);
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        UUID serviceId = (UUID) variables.get(RecreateConstants.SERVICE_ID);
        destroyTask.setOriginalServiceId(serviceId);
        destroyTask.setServiceId(serviceId);
        UUID recreateOrderId = (UUID) variables.get(RecreateConstants.RECREATE_ORDER_ID);
        destroyTask.setParentOrderId(recreateOrderId);
        String userId = (String) variables.get(RecreateConstants.USER_ID);
        destroyTask.setUserId(userId);
        return destroyTask;
    }
}
