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
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Service porting process destroy service processing class. */
@Slf4j
@Component
public class StartDestroy implements Serializable, JavaDelegate {

    private final DeployService deployService;
    private final RuntimeService runtimeService;

    /** Constructor for StartDestroy bean. */
    @Autowired
    public StartDestroy(DeployService deployService, RuntimeService runtimeService) {
        this.deployService = deployService;
        this.runtimeService = runtimeService;
    }

    /** Methods when performing destroy tasks. */
    @SneakyThrows
    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        UUID originalServiceId = (UUID) variables.get(ServicePortingConstants.ORIGINAL_SERVICE_ID);
        UUID servicePortingOrderId =
                (UUID) variables.get(ServicePortingConstants.SERVICE_PORTING_ORDER_ID);
        int retryTimes = (int) variables.get(ServicePortingConstants.DESTROY_RETRY_NUM);
        log.info(
                "Start destroy task in service porting workflow with id:{}.Retry times:{}",
                processInstanceId,
                retryTimes);
        ServiceOrder serviceOrder =
                deployService.destroyServiceByWorkflow(
                        originalServiceId, processInstanceId, servicePortingOrderId);
        log.info("Started new destroy task with order: {} successfully.", serviceOrder.toString());
    }
}
