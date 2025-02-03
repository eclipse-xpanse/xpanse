package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.xpanse.modules.deployment.serviceporting.consts.ServicePortingConstants.IS_RETRY_TASK;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.workflow.WorkFlowTask;
import org.eclipse.xpanse.modules.models.workflow.WorkFlowTaskStatus;
import org.eclipse.xpanse.modules.workflow.utils.WorkflowUtils;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = {XpanseApplication.class, WorkFlowApiTest.class, WorkflowUtils.class},
        properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test,dev"})
@AutoConfigureMockMvc
class WorkFlowApiTest extends ApisTestCommon {

    @MockitoBean private WorkflowUtils mockWorkflowUtils;

    @Test
    @WithJwt("jwt_user.json")
    void testQueryTasksWithStatus() throws Exception {
        testQueryTasks();
        testManageFailedOrder();
        testCompleteTask();
    }

    WorkFlowTask getTaskWithStatus(WorkFlowTaskStatus status) {
        String processInstanceId = UUID.randomUUID().toString();
        String processDefinitionKey = "servicePorting";
        WorkFlowTask task = new WorkFlowTask();
        task.setProcessInstanceId(processInstanceId);
        task.setProcessDefinitionId(processDefinitionKey);
        task.setProcessDefinitionName(processDefinitionKey);
        String taskId = UUID.randomUUID().toString();
        task.setStatus(status);
        task.setTaskId(taskId);
        task.setExecutionId(taskId);
        task.setTaskName("ExportData");
        task.setBusinessKey(processInstanceId);
        task.setCreateTime(OffsetDateTime.now());
        return task;
    }

    void testQueryTasks() throws Exception {
        String userId = "userId";
        WorkFlowTask todoTask = getTaskWithStatus(WorkFlowTaskStatus.FAILED);
        WorkFlowTask doneTask = getTaskWithStatus(WorkFlowTaskStatus.DONE);
        // Setup
        when(mockWorkflowUtils.queryAllTasks(WorkFlowTaskStatus.DONE, userId))
                .thenReturn(List.of(doneTask));
        when(mockWorkflowUtils.queryAllTasks(WorkFlowTaskStatus.FAILED, userId))
                .thenReturn(List.of(todoTask));
        when(mockWorkflowUtils.queryAllTasks(null, userId)).thenReturn(List.of(doneTask, todoTask));
        // Run the test
        final MockHttpServletResponse taskResponse =
                mockMvc.perform(
                                get("/xpanse/workflow/tasks")
                                        .param("status", "DONE")
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertThat(taskResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(taskResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(List.of(doneTask)));

        // Run the test
        final MockHttpServletResponse todoTaskResponse =
                mockMvc.perform(
                                get("/xpanse/workflow/tasks")
                                        .param("status", "failed")
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertThat(todoTaskResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(todoTaskResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(List.of(todoTask)));

        // Run the test
        final MockHttpServletResponse allTaskResponse =
                mockMvc.perform(get("/xpanse/workflow/tasks").accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertThat(allTaskResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(allTaskResponse.getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(List.of(doneTask, todoTask)));
    }

    void testCompleteTask() throws Exception {
        // Setup
        String taskId = UUID.randomUUID().toString();
        Map<String, Object> variables = Map.ofEntries(Map.entry("key", "value"));
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                put("/xpanse/workflow/complete/task/{id}", taskId)
                                        .content(objectMapper.writeValueAsString(variables))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("");
        verify(mockWorkflowUtils).completeTask(taskId, variables);
    }

    void testManageFailedOrder() throws Exception {
        // Setup
        String taskId = UUID.randomUUID().toString();
        // Run the test
        final MockHttpServletResponse response =
                mockMvc.perform(
                                put("/xpanse/workflow/task/{id}", taskId)
                                        .param("retryOrder", "true")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("");
        verify(mockWorkflowUtils)
                .completeTask(taskId, Map.ofEntries(Map.entry(IS_RETRY_TASK, true)));
    }
}
