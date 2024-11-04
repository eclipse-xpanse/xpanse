/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.migration.steps;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.deployment.migration.consts.MigrateConstants;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Migration process destroy service processing class.
 */
@Slf4j
@Component
public class StartDestroy implements Serializable, JavaDelegate {

    private final DeployService deployService;
    private final RuntimeService runtimeService;

    /**
     * Constructor for StartDestroy bean.
     */
    @Autowired
    public StartDestroy(DeployService deployService, RuntimeService runtimeService) {
        this.deployService = deployService;
        this.runtimeService = runtimeService;
    }

    /**
     * Methods when performing destroy tasks.
     */
    @SneakyThrows
    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        UUID originalServiceId = (UUID) variables.get(MigrateConstants.ORIGINAL_SERVICE_ID);
        UUID migrateOrderId = (UUID) variables.get(MigrateConstants.MIGRATE_ORDER_ID);
        int retryTimes = (int) variables.get(MigrateConstants.DESTROY_RETRY_NUM);
        log.info("Start destroy task in migration workflow with id:{}.Retry times:{}",
                processInstanceId, retryTimes);
        ServiceOrder serviceOrder = deployService.destroyServiceByWorkflow(originalServiceId,
                processInstanceId, migrateOrderId);
        log.info("Started new destroy task with order: {} successfully.", serviceOrder.toString());
    }
}
