/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.recreate.steps;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.eclipse.xpanse.modules.deployment.recreate.consts.RecreateConstants;
import org.springframework.stereotype.Component;

/** Recreate monitoring class for manual recreation processing after destroy failure and retry. */
@Slf4j
@Component
public class RecreateDestroyFailedManualHandlerListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution delegateExecution) {
        String processInstanceId = delegateExecution.getProcessInstanceId();
        log.info(
                "Start manually handler destroy failed for recreation workflow. "
                        + "ProcessInstanceId:{}",
                processInstanceId);
        Map<String, Object> variables = delegateExecution.getVariables();
        variables.put(RecreateConstants.DESTROY_RETRY_NUM, 0);
        delegateExecution.setVariables(variables);
    }
}
