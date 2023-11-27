/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.workflow.handle.migrate;

import java.io.Serializable;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.eclipse.xpanse.modules.workflow.consts.MigrateConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Migration process export data processing class.
 */
@Slf4j
@Component
public class MigrateExportDataProcess implements Serializable, JavaDelegate {

    private static RuntimeService runtimeService;

    @Autowired
    public void setRuntimeService(RuntimeService runtimeService) {
        MigrateExportDataProcess.runtimeService = runtimeService;
    }

    /**
     * Migration process, export data link business logic(Not yet developed).
     */
    @Override
    public void execute(DelegateExecution delegateExecution) {
        String processInstanceId = delegateExecution.getProcessInstanceId();
        runtimeService.setVariable(processInstanceId, MigrateConstants.DEPLOY_RETRY_NUM, 0);
        //TODO Export data process not yet implemented. Skipping.
        log.info("start export data.ProcessInstanceId:{}",
                delegateExecution.getProcessInstanceId());

    }
}
