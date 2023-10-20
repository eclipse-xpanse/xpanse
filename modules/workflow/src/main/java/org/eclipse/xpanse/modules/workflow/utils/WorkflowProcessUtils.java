/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.workflow.utils;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;
import org.eclipse.xpanse.modules.models.workflow.WorkFlowTask;
import org.eclipse.xpanse.modules.models.workflow.enums.MigrateState;
import org.eclipse.xpanse.modules.workflow.consts.MigrateConstants;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Process tool class.
 */
@Component
public class WorkflowProcessUtils {

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    @Resource
    private HistoryService historyService;

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

    /**
     * Get the user's to-do tasks through the ID of the currently logged in user.
     *
     * @param userId the ID of the currently logged in user.
     */
    public List<WorkFlowTask> todoTasks(String userId) {
        List<Task> list = taskService.createTaskQuery().taskAssignee(userId).list();
        return transTaskToWorkFlowTask(list);
    }

    /**
     * Get the tasks that the user has done through the ID of the currently logged in user.
     *
     * @param userId the ID of the currently logged in user.
     */
    public List<WorkFlowTask> doneTasks(String userId) {
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .finished()
                .list();
        return transHistoricTaskInstanceToWorkFlowTask(list);
    }

    /**
     * Query process status.
     *
     * @param processInstanceId Process instance ID
     */
    public String getWorkFlowState(String processInstanceId) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        List<HistoricVariableInstance> list = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId).list();
        Map<String, Object> variablesMap = new HashMap<>();
        for (HistoricVariableInstance variableInstance : list) {
            variablesMap.put(variableInstance.getVariableName(), variableInstance.getValue());
        }

        if (instance == null) {
            if (variablesMap.containsKey(MigrateConstants.IS_RETRY_TASK)
                    && !(boolean) variablesMap.get(MigrateConstants.IS_RETRY_TASK)) {
                return MigrateState.MIGRATION_FAILED.toValue();
            } else {
                return MigrateState.MIGRATION_SUCCESS.toValue();
            }
        }
        return MigrateState.MIGRATING.toValue();
    }

    /**
     * Complete tasks based on task ID and set global process variables.
     *
     * @param taskId    taskId taskId.
     * @param variables global process variables.
     */
    public void completeTask(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
    }


    private WorkFlowTask getWorkFlow(TaskInfo task) {
        WorkFlowTask workFlowTask = new WorkFlowTask();
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).singleResult();
        workFlowTask.setProcessInstanceId(task.getProcessInstanceId());
        workFlowTask.setProcessInstanceName(instance.getProcessDefinitionName());
        workFlowTask.setProcessDefinitionId(task.getProcessDefinitionId());
        workFlowTask.setProcessDefinitionName(instance.getProcessDefinitionName());
        workFlowTask.setExecutionId(task.getExecutionId());
        workFlowTask.setTaskId(task.getId());
        workFlowTask.setTaskName(task.getName());
        workFlowTask.setBusinessKey(instance.getBusinessKey());
        workFlowTask.setCreateTime(task.getCreateTime());
        return workFlowTask;
    }

    private List<WorkFlowTask> transTaskToWorkFlowTask(List<Task> list) {
        return list.stream().map(task -> getWorkFlow(task)).collect(Collectors.toList());
    }

    private List<WorkFlowTask> transHistoricTaskInstanceToWorkFlowTask(
            List<HistoricTaskInstance> list) {
        return list.stream().map(task -> getWorkFlow(task)).collect(Collectors.toList());
    }
}
