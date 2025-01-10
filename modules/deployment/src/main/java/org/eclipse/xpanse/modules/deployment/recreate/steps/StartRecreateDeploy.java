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
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.workflow.recreate.exceptions.ServiceRecreateFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Recreate process deployment service processing class. */
@Slf4j
@Component
public class StartRecreateDeploy implements Serializable, JavaDelegate {

    @Serial private static final long serialVersionUID = 2725212494105579585L;

    private final DeployService deployService;
    private final RuntimeService runtimeService;
    private final ServiceOrderManager serviceOrderManager;

    /** Constructor for StartRecreateDeploy bean. */
    @Autowired
    public StartRecreateDeploy(
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
        UUID serviceId = (UUID) variables.get(RecreateConstants.SERVICE_ID);
        UUID recreateOrderId = (UUID) variables.get(RecreateConstants.RECREATE_ORDER_ID);
        log.info(
                "Recreate workflow of instance id : {} start deploy new service with id:{}",
                processInstanceId,
                serviceId.toString());
        try {
            startDeploy(processInstanceId, serviceId, recreateOrderId, variables);
        } catch (ServiceRecreateFailedException e) {
            log.info(
                    "Recreate workflow of instance id : {} start deploy new service with id: {},"
                            + " error: {}",
                    processInstanceId,
                    serviceId,
                    e.getMessage());
            runtimeService.setVariable(
                    processInstanceId, RecreateConstants.IS_DEPLOY_SUCCESS, false);
            serviceOrderManager.completeOrderProgress(
                    recreateOrderId,
                    TaskStatus.FAILED,
                    ErrorResponse.errorResponse(
                            ErrorType.DEPLOYMENT_FAILED_EXCEPTION, List.of(e.getMessage())));
        }
    }

    private void startDeploy(
            String processInstanceId,
            UUID originalServiceId,
            UUID recreateOrderId,
            Map<String, Object> variables) {
        runtimeService.updateBusinessKey(processInstanceId, originalServiceId.toString());
        String userId = (String) variables.get(RecreateConstants.USER_ID);
        DeployRequest deployRequest =
                (DeployRequest) variables.get(RecreateConstants.RECREATE_REQUEST);
        deployRequest.setUserId(userId);
        deployService.deployServiceByWorkflow(
                originalServiceId, processInstanceId, recreateOrderId, deployRequest);
    }
}
