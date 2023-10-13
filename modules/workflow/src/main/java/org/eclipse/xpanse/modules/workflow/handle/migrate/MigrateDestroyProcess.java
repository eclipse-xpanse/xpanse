/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.workflow.handle.migrate;

import java.io.Serializable;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Migration process destroy service processing class.
 */
@Slf4j
@Component
public class MigrateDestroyProcess implements Serializable, JavaDelegate {

    private static RuntimeService runtimeService;
    private static DeployService deployService;

    @Autowired
    public void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Autowired
    public void setDeployService(DeployService deployService) {
        this.deployService = deployService;
    }

    /**
     * Methods when performing destroy tasks.
     */
    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        String id = (String) variables.get("id");
        boolean isDestroySuccess = deployService.destroyService(id);
        runtimeService.setVariable(processInstanceId, "isDestroySuccess", isDestroySuccess);
    }
}
