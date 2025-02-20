/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.serviceporting.steps;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.deployment.serviceporting.consts.ServicePortingConstants;
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.workflow.WorkFlowDeployRequest;
import org.eclipse.xpanse.modules.models.workflow.serviceporting.ServicePortingRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Service porting process deployment service processing class. */
@Slf4j
@Component
public class StartDeploy implements Serializable, JavaDelegate {

    private final DeployService deployService;
    private final RuntimeService runtimeService;

    /** Constructor for StartDeploy bean. */
    @Autowired
    public StartDeploy(DeployService deployService, RuntimeService runtimeService) {
        this.deployService = deployService;
        this.runtimeService = runtimeService;
    }

    /** Methods when performing deployment tasks. */
    @SneakyThrows
    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        ServicePortingRequest servicePortingRequest =
                (ServicePortingRequest)
                        variables.get(ServicePortingConstants.SERVICE_PORTING_REQUEST);
        DeployRequest deployRequest = new DeployRequest();
        BeanUtils.copyProperties(servicePortingRequest, deployRequest);
        int retryTimes = (int) variables.get(ServicePortingConstants.DEPLOY_RETRY_NUM);
        log.info(
                "Start deploy task in service porting workflow with id:{}. Request:{}. Retry"
                        + " times:{}",
                processInstanceId,
                deployRequest,
                retryTimes);
        UUID originalServiceId = (UUID) variables.get(ServicePortingConstants.ORIGINAL_SERVICE_ID);
        UUID newServiceId = (UUID) variables.get(ServicePortingConstants.NEW_SERVICE_ID);
        String userId = (String) variables.get(ServicePortingConstants.USER_ID);
        UUID servicePortingOrderId =
                (UUID) variables.get(ServicePortingConstants.SERVICE_PORTING_ORDER_ID);
        WorkFlowDeployRequest workFlowDeployRequest = new WorkFlowDeployRequest();
        workFlowDeployRequest.setOriginalServiceId(originalServiceId);
        workFlowDeployRequest.setNewServiceId(newServiceId);
        workFlowDeployRequest.setParentOrderId(servicePortingOrderId);
        workFlowDeployRequest.setWorkflowId(processInstanceId);
        workFlowDeployRequest.setUserId(userId);
        workFlowDeployRequest.setDeployRequest(deployRequest);
        ServiceOrder serviceOrder = deployService.deployServiceByWorkflow(workFlowDeployRequest);
        log.info("Started new deploy task with order: {} successfully.", serviceOrder.toString());
    }
}
