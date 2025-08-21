/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.serviceporting.steps;

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
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;
import org.eclipse.xpanse.modules.models.service.enums.OrderStatus;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.workflow.serviceporting.ServicePortingRequest;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Service porting process deployment service processing class. */
@Slf4j
@Component
public class StartDeploy implements JavaDelegate {

    private final transient DeployService deployService;
    private final transient RuntimeService runtimeService;
    private final transient ServiceOrderManager serviceOrderManager;

    /** Constructor for StartDeploy bean. */
    @Autowired
    public StartDeploy(
            DeployService deployService,
            RuntimeService runtimeService,
            ServiceOrderManager serviceOrderManager) {
        this.deployService = deployService;
        this.runtimeService = runtimeService;
        this.serviceOrderManager = serviceOrderManager;
    }

    /** Methods when performing deployment tasks. */
    @SneakyThrows
    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        DeployTask deployTask = getDeployTaskForPorting(processInstanceId, variables);
        int retryTimes = (int) variables.get(ServicePortingConstants.DEPLOY_RETRY_NUM);
        log.info(
                "Start deploy task in service porting workflow with id:{}. Task:{}. Retry times:{}",
                processInstanceId,
                deployTask,
                retryTimes);
        try {
            ServiceOrder serviceOrder = deployService.deployServiceByWorkflow(deployTask);
            runtimeService.setVariable(
                    processInstanceId,
                    ServicePortingConstants.NEW_SERVICE_ID,
                    serviceOrder.getServiceId());
            log.info("Started deploy task with order: {} successfully.", serviceOrder);
        } catch (Exception e) {
            log.error(
                    "Started deploy task in service porting workflow with id:{} failed. ",
                    processInstanceId,
                    e);
            runtimeService.setVariable(
                    processInstanceId, ServicePortingConstants.IS_DEPLOY_SUCCESS, false);
            serviceOrderManager.completeOrderProgress(
                    deployTask.getParentOrderId(),
                    OrderStatus.FAILED,
                    ErrorResponse.errorResponse(
                            ErrorType.DEPLOYMENT_FAILED_EXCEPTION, List.of(e.getMessage())));
        }
    }

    private DeployTask getDeployTaskForPorting(
            String processInstanceId, Map<String, Object> variables) {
        DeployTask deployTask = new DeployTask();
        deployTask.setWorkflowId(processInstanceId);
        UUID originalServiceId = (UUID) variables.get(ServicePortingConstants.ORIGINAL_SERVICE_ID);
        deployTask.setOriginalServiceId(originalServiceId);
        UUID servicePortingOrderId =
                (UUID) variables.get(ServicePortingConstants.SERVICE_PORTING_ORDER_ID);
        deployTask.setParentOrderId(servicePortingOrderId);
        String userId = (String) variables.get(ServicePortingConstants.USER_ID);
        deployTask.setUserId(userId);
        ServicePortingRequest servicePortingRequest =
                (ServicePortingRequest)
                        variables.get(ServicePortingConstants.SERVICE_PORTING_REQUEST);
        DeployRequest deployRequest = new DeployRequest();
        BeanUtils.copyProperties(servicePortingRequest, deployRequest);
        deployTask.setDeployRequest(deployRequest);
        deployTask.setRequest(servicePortingRequest);
        deployTask.setTaskType(ServiceOrderType.PORT);
        return deployTask;
    }
}
