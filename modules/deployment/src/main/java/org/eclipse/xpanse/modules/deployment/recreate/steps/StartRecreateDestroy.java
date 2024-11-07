/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.recreate.steps;

import java.io.Serial;
import java.io.Serializable;
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
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.workflow.recreate.exceptions.ServiceRecreateFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Recreate process deployment service processing class.
 */
@Slf4j
@Component
public class StartRecreateDestroy implements Serializable, JavaDelegate {


    @Serial
    private static final long serialVersionUID = 2725212494105579585L;

    private final DeployService deployService;
    private final ServiceOrderManager serviceOrderManager;
    private final RuntimeService runtimeService;

    /**
     * Constructor for StartRecreateDeploy bean.
     */
    @Autowired
    public StartRecreateDestroy(DeployService deployService,
                                ServiceOrderManager serviceOrderManager,
                                RuntimeService runtimeService) {
        this.deployService = deployService;
        this.serviceOrderManager = serviceOrderManager;
        this.runtimeService = runtimeService;
    }

    /**
     * Methods when performing deployment tasks.
     */
    @SneakyThrows
    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();

        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        UUID serviceId = (UUID) variables.get(RecreateConstants.SERVICE_ID);
        UUID recreateOrderId = (UUID) variables.get(RecreateConstants.RECREATE_ORDER_ID);
        int retryDestroyTimes = (int) variables.get(RecreateConstants.DESTROY_RETRY_NUM);
        log.info(
                "Recreate workflow of instance id : {} start destroy old service with id:{}. "
                        + "Retry times:{}",
                processInstanceId, serviceId, retryDestroyTimes);
        try {

            deployService.destroyServiceByWorkflow(serviceId, processInstanceId, recreateOrderId);
        } catch (ServiceRecreateFailedException e) {
            log.error("Recreate workflow of instance id : {} start destroy old service with id: {},"
                    + " error: {}", processInstanceId, serviceId, e.getMessage());
            runtimeService.setVariable(processInstanceId, RecreateConstants.IS_DESTROY_SUCCESS,
                    false);
            serviceOrderManager.completeOrderProgress(recreateOrderId, TaskStatus.FAILED,
                    e.getMessage());
        }

    }
}

