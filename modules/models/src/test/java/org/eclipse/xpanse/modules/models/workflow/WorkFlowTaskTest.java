/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkFlowTaskTest {

    @Mock private OffsetDateTime mockCreatedTime;

    @Test
    void testProcessInstanceIdGetterAndSetter() {
        final WorkFlowTask workFlowTaskUnderTest = new WorkFlowTask();
        workFlowTaskUnderTest.setCreatedTime(mockCreatedTime);
        final String processInstanceId = "processInstanceId";
        workFlowTaskUnderTest.setProcessInstanceId(processInstanceId);
        assertThat(workFlowTaskUnderTest.getProcessInstanceId()).isEqualTo(processInstanceId);
    }

    @Test
    void testProcessInstanceNameGetterAndSetter() {
        final WorkFlowTask workFlowTaskUnderTest = new WorkFlowTask();
        workFlowTaskUnderTest.setCreatedTime(mockCreatedTime);
        final String processInstanceName = "processInstanceName";
        workFlowTaskUnderTest.setProcessInstanceName(processInstanceName);
        assertThat(workFlowTaskUnderTest.getProcessInstanceName()).isEqualTo(processInstanceName);
    }

    @Test
    void testProcessDefinitionIdGetterAndSetter() {
        final WorkFlowTask workFlowTaskUnderTest = new WorkFlowTask();
        workFlowTaskUnderTest.setCreatedTime(mockCreatedTime);
        final String processDefinitionId = "processDefinitionId";
        workFlowTaskUnderTest.setProcessDefinitionId(processDefinitionId);
        assertThat(workFlowTaskUnderTest.getProcessDefinitionId()).isEqualTo(processDefinitionId);
    }

    @Test
    void testProcessDefinitionNameGetterAndSetter() {
        final WorkFlowTask workFlowTaskUnderTest = new WorkFlowTask();
        workFlowTaskUnderTest.setCreatedTime(mockCreatedTime);
        final String processDefinitionName = "processDefinitionName";
        workFlowTaskUnderTest.setProcessDefinitionName(processDefinitionName);
        assertThat(workFlowTaskUnderTest.getProcessDefinitionName())
                .isEqualTo(processDefinitionName);
    }

    @Test
    void testExecutionIdGetterAndSetter() {
        final WorkFlowTask workFlowTaskUnderTest = new WorkFlowTask();
        workFlowTaskUnderTest.setCreatedTime(mockCreatedTime);
        final String executionId = "executionId";
        workFlowTaskUnderTest.setExecutionId(executionId);
        assertThat(workFlowTaskUnderTest.getExecutionId()).isEqualTo(executionId);
    }

    @Test
    void testTaskIdGetterAndSetter() {
        final WorkFlowTask workFlowTaskUnderTest = new WorkFlowTask();
        workFlowTaskUnderTest.setCreatedTime(mockCreatedTime);
        final String taskId = "taskId";
        workFlowTaskUnderTest.setTaskId(taskId);
        assertThat(workFlowTaskUnderTest.getTaskId()).isEqualTo(taskId);
    }

    @Test
    void testTaskNameGetterAndSetter() {
        final WorkFlowTask workFlowTaskUnderTest = new WorkFlowTask();
        workFlowTaskUnderTest.setCreatedTime(mockCreatedTime);
        final String taskName = "taskName";
        workFlowTaskUnderTest.setTaskName(taskName);
        assertThat(workFlowTaskUnderTest.getTaskName()).isEqualTo(taskName);
    }

    @Test
    void testBusinessKeyGetterAndSetter() {
        final WorkFlowTask workFlowTaskUnderTest = new WorkFlowTask();
        workFlowTaskUnderTest.setCreatedTime(mockCreatedTime);
        final String businessKey = "businessKey";
        workFlowTaskUnderTest.setBusinessKey(businessKey);
        assertThat(workFlowTaskUnderTest.getBusinessKey()).isEqualTo(businessKey);
    }

    @Test
    void testGetCreatedTime() {
        final WorkFlowTask workFlowTaskUnderTest = new WorkFlowTask();
        mockCreatedTime = OffsetDateTime.now();
        workFlowTaskUnderTest.setCreatedTime(mockCreatedTime);
        assertThat(workFlowTaskUnderTest.getCreatedTime()).isEqualTo(mockCreatedTime);
    }

    @Test
    void testCanEqual() {
        final WorkFlowTask workFlowTaskUnderTest = new WorkFlowTask();
        workFlowTaskUnderTest.setCreatedTime(mockCreatedTime);
        assertThat(workFlowTaskUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        final WorkFlowTask workFlowTaskUnderTest = new WorkFlowTask();
        workFlowTaskUnderTest.setCreatedTime(mockCreatedTime);

        final WorkFlowTask workFlowTaskUnderTest2 = new WorkFlowTask();
        workFlowTaskUnderTest2.setCreatedTime(mockCreatedTime);
        assertThat(workFlowTaskUnderTest.hashCode()).isEqualTo(workFlowTaskUnderTest2.hashCode());
    }

    @Test
    void testToString() {
        String result =
                "WorkFlowTask(processInstanceId=null, processInstanceName=null,"
                        + " processDefinitionId=null, processDefinitionName=null, executionId=null,"
                        + " taskId=null, taskName=null, businessKey=null, taskStatus=null, "
                        + "createdTime=mockCreatedTime)";

        final WorkFlowTask workFlowTaskUnderTest = new WorkFlowTask();
        workFlowTaskUnderTest.setCreatedTime(mockCreatedTime);
        assertThat(workFlowTaskUnderTest.toString()).isEqualTo(result);
    }
}
