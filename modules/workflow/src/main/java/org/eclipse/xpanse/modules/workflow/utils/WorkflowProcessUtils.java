/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.workflow.utils;

import java.util.Map;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Process tool class.
 */
@Component
public class WorkflowProcessUtils {

    @Autowired
    private RuntimeService runtimeService;

    /**
     * Start the process through the process definition key.
     *
     * @param processKey process definition key.
     * @return ProcessInstance Process instance.
     */
    public ProcessInstance startProcess(String processKey) {
        return startProcess(processKey, null);
    }

    /**
     * Start the process through the process definition key and set the process variables.
     *
     * @param processKey process definition key.
     * @param variable   Process variables.
     * @return ProcessInstance Process instance.
     */
    public ProcessInstance startProcess(String processKey, Map<String, Object> variable) {
        return runtimeService.startProcessInstanceByKey(processKey, variable);
    }

    @Async("taskExecutor")
    public void asyncStartProcess(String processKey, Map<String, Object> variable) {
        startProcess(processKey, variable);
    }
}
