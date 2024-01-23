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
 * Monitoring class for manual processing after destroy failure and retry.
 */
@Slf4j
@Component
public class DestroyFailedManualHandlerListener implements ExecutionListener {

    private static RuntimeService runtimeService;

    @Autowired
    public void setRuntimeService(RuntimeService runtimeService) {
        DestroyFailedManualHandlerListener.runtimeService = runtimeService;
    }

    @Override
    public void notify(DelegateExecution delegateExecution) {
        String processInstanceId = delegateExecution.getProcessInstanceId();
        log.info("Start Manually Handler Destroy Failed. ProcessInstanceId:{}",
                processInstanceId);
        runtimeService.setVariable(processInstanceId, MigrateConstants.DESTROY_RETRY_NUM, 0);
    }
}
