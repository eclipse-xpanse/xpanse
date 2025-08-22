/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.serviceporting.steps;

import java.io.Serial;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.eclipse.xpanse.modules.deployment.serviceporting.consts.ServicePortingConstants;
import org.springframework.stereotype.Component;

/** Monitoring class for manual processing after deployment failure and retry. */
@Slf4j
@Component
public class DeployFailedManualHandlerListener implements ExecutionListener {

    @Serial private static final long serialVersionUID = 202406121002L;

    @Override
    public void notify(DelegateExecution delegateExecution) {
        String processInstanceId = delegateExecution.getProcessInstanceId();
        log.info("Start Manually Handler Deploy Failed. ProcessInstanceId:{}", processInstanceId);
        Map<String, Object> variables = delegateExecution.getVariables();
        variables.put(ServicePortingConstants.DEPLOY_RETRY_NUM, 0);
        delegateExecution.setVariables(variables);
    }
}
