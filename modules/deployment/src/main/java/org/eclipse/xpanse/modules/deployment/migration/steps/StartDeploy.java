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
import org.eclipse.xpanse.modules.models.service.deployment.DeployRequest;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.workflow.migrate.MigrateRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Migration process deployment service processing class. */
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
        MigrateRequest migrateRequest =
                (MigrateRequest) variables.get(MigrateConstants.MIGRATE_REQUEST);
        DeployRequest deployRequest = new DeployRequest();
        UUID newServiceId = (UUID) variables.get(MigrateConstants.NEW_SERVICE_ID);
        BeanUtils.copyProperties(migrateRequest, deployRequest);
        deployRequest.setServiceId(newServiceId);
        int retryTimes = (int) variables.get(MigrateConstants.DEPLOY_RETRY_NUM);
        log.info(
                "Start deploy task in migration workflow with id:{}. Request:{}. Retry times:{}",
                processInstanceId,
                deployRequest,
                retryTimes);
        UUID originalServiceId = (UUID) variables.get(MigrateConstants.ORIGINAL_SERVICE_ID);
        UUID migrateOrderId = (UUID) variables.get(MigrateConstants.MIGRATE_ORDER_ID);
        ServiceOrder serviceOrder =
                deployService.deployServiceByWorkflow(
                        originalServiceId, processInstanceId, migrateOrderId, deployRequest);
        log.info("Started new deploy task with order: {} successfully.", serviceOrder.toString());
    }
}
