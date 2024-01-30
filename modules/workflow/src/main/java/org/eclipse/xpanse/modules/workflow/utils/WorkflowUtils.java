/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.workflow.utils;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceNotDeployedException;
import org.eclipse.xpanse.modules.models.workflow.TaskStatus;
import org.eclipse.xpanse.modules.models.workflow.WorkFlowTask;
import org.springframework.stereotype.Component;

/**
 * Process tool class.
 */
@Component
public class WorkflowUtils {

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
     * Query all tasks of the given user.
     *
     * @param userId userId the ID of the currently logged in user.
     */
    public List<WorkFlowTask> queryAllTasks(TaskStatus status, String userId) {
        List<WorkFlowTask> workFlowTasks = new ArrayList<>();
        List<WorkFlowTask> todoTasks = todoTasks(userId);
        List<WorkFlowTask> doneTasks = doneTasks(userId);
        if (Objects.isNull(status)) {
            workFlowTasks.addAll(todoTasks.stream().map(this::setTodoTaskStatus).toList());
            workFlowTasks.addAll(doneTasks.stream().map(this::setDoneTaskStatus).toList());
        } else if (status == TaskStatus.DONE) {
            workFlowTasks.addAll(doneTasks.stream().map(this::setDoneTaskStatus).toList());
        } else if (status == TaskStatus.FAILED) {
            workFlowTasks.addAll(todoTasks.stream().map(this::setTodoTaskStatus).toList());
        }
        return workFlowTasks;
    }

    /**
     * Complete tasks based on task ID and set global process variables.
     *
     * @param taskId    taskId taskId.
     * @param variables global process variables.
     */
    public void completeTask(String taskId, Map<String, Object> variables) {
        validateTaskId(taskId);
        taskService.complete(taskId, variables);
    }

    /**
     * Complete ReceiveTask for the given processInstanceId and activityId.
     * The method closes the waiting task and pushes the workflow to the next step.
     */
    public void completeReceiveTask(String processInstanceId, String activityId) {
        if (StringUtils.isNotBlank(processInstanceId)) {
            ProcessInstance instance =
                    runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
                            .singleResult();

            if (Objects.nonNull(instance)) {
                Execution execution = runtimeService.createExecutionQuery()
                        .processInstanceId(processInstanceId)
                        .activityId(activityId)
                        .singleResult();
                runtimeService.trigger(execution.getId());

            }
        }
    }

    private WorkFlowTask getWorkFlow(TaskInfo task) {
        WorkFlowTask workFlowTask = new WorkFlowTask();
        workFlowTask.setProcessInstanceId(task.getProcessInstanceId());
        workFlowTask.setProcessDefinitionId(task.getProcessDefinitionId());
        workFlowTask.setExecutionId(task.getExecutionId());
        workFlowTask.setTaskId(task.getId());
        workFlowTask.setTaskName(task.getName());
        workFlowTask.setCreateTime(task.getCreateTime());
        return workFlowTask;
    }

    private WorkFlowTask getTodoWorkFlow(TaskInfo task) {
        WorkFlowTask workFlowTask = getWorkFlow(task);
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).singleResult();
        if (Objects.nonNull(instance)) {
            workFlowTask.setProcessInstanceName(instance.getProcessDefinitionName());
            workFlowTask.setProcessDefinitionName(instance.getProcessDefinitionName());
            workFlowTask.setBusinessKey(instance.getBusinessKey());
        }
        return workFlowTask;
    }


    private WorkFlowTask getDoneWorkFlow(TaskInfo task) {
        WorkFlowTask workFlowTask = getWorkFlow(task);
        HistoricProcessInstance instance =
                historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(task.getProcessInstanceId()).singleResult();
        if (Objects.nonNull(instance)) {
            workFlowTask.setProcessInstanceName(instance.getProcessDefinitionName());
            workFlowTask.setProcessDefinitionName(instance.getProcessDefinitionName());
            workFlowTask.setBusinessKey(instance.getBusinessKey());
        }
        return workFlowTask;
    }

    private List<WorkFlowTask> transTaskToWorkFlowTask(List<Task> list) {
        return list.stream().map(this::getTodoWorkFlow).collect(Collectors.toList());
    }

    private List<WorkFlowTask> transHistoricTaskInstanceToWorkFlowTask(
            List<HistoricTaskInstance> list) {
        return list.stream().map(this::getDoneWorkFlow).collect(Collectors.toList());
    }

    private void validateTaskId(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new ServiceNotDeployedException("The migrated activiti task was not found, "
                    + "taskId: " + taskId);
        }
    }

    private WorkFlowTask setTodoTaskStatus(WorkFlowTask workFlowTask) {
        workFlowTask.setStatus(TaskStatus.FAILED);
        return workFlowTask;
    }

    private WorkFlowTask setDoneTaskStatus(WorkFlowTask workFlowTask) {
        workFlowTask.setStatus(TaskStatus.DONE);
        return workFlowTask;
    }
}
