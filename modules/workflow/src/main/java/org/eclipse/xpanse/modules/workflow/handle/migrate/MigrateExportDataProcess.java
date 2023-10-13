/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.workflow.handle.migrate;

import java.io.Serializable;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * Migration process export data processing class.
 */
@Slf4j
@Component
public class MigrateExportDataProcess implements Serializable, JavaDelegate {

    /**
     * Migration process, export data link business logic(Not yet developed).
     */
    @Override
    public void execute(DelegateExecution delegateExecution) {
        //TODO Export data process not yet implemented. Skipping.
        log.info("start export data.ProcessInstanceId:{}",
                delegateExecution.getProcessInstanceId());

    }
}
