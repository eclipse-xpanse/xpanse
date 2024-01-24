/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.migration.steps;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.eclipse.xpanse.modules.deployment.migration.consts.MigrateConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Monitoring class for manual processing after deployment failure and retry.
 */
@Slf4j
@Component
public class DeployFailedManualHandlerListener implements ExecutionListener {

    private final RuntimeService runtimeService;

    @Autowired
    public DeployFailedManualHandlerListener(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Override
    public void notify(DelegateExecution delegateExecution) {
        String processInstanceId = delegateExecution.getProcessInstanceId();
        log.info("Start Manually Handler Deploy Failed. ProcessInstanceId:{}",
                processInstanceId);
        runtimeService.setVariable(processInstanceId, MigrateConstants.DEPLOY_RETRY_NUM, 0);
    }
}
