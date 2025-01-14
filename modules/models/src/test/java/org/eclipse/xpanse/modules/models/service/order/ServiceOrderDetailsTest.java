package org.eclipse.xpanse.modules.models.service.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class ServiceOrderDetailsTest {
    private final OffsetDateTime startedTime =
            OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
    private final OffsetDateTime completedTime =
            OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
    private final UUID serviceId = UUID.fromString("168b2be0-3535-4042-b53c-23cabd874a51");
    private final UUID orderId = UUID.fromString("4caabd86-1967-4351-aedc-b18cbab3ab61");
    private final UUID originalServiceId = UUID.fromString("4caabd86-1967-4351-aedc-b18cbab3ab62");
    private final UUID parentOrderId = UUID.fromString("4caabd86-1967-4351-aedc-b18cbab3ab63");
    private final String workflowId = "workflowId";
    private final ErrorResponse errorResponse = new ErrorResponse();
    private final String userId = "userId";
    private final TaskStatus taskStatus = TaskStatus.SUCCESSFUL;
    private final ServiceOrderType taskType = ServiceOrderType.DEPLOY;
    @Mock private Map<String, Object> mockRequestBody;
    @Mock private Map<String, Object> mockResultProperties;

    private ServiceOrderDetails test;

    @BeforeEach
    void setUp() {
        test = new ServiceOrderDetails();
        test.setOrderId(orderId);
        test.setServiceId(serviceId);
        test.setOriginalServiceId(originalServiceId);
        test.setParentOrderId(parentOrderId);
        test.setWorkflowId(workflowId);
        test.setTaskType(taskType);
        test.setUserId(userId);
        test.setStartedTime(startedTime);
        test.setCompletedTime(completedTime);
        test.setErrorResponse(errorResponse);
        test.setTaskStatus(taskStatus);
        test.setRequestBody(mockRequestBody);
        test.setResultProperties(mockResultProperties);
    }

    @Test
    void testGetters() {
        assertThat(test.getOrderId()).isEqualTo(orderId);
        assertThat(test.getServiceId()).isEqualTo(serviceId);
        assertThat(test.getOriginalServiceId()).isEqualTo(originalServiceId);
        assertThat(test.getParentOrderId()).isEqualTo(parentOrderId);
        assertThat(test.getWorkflowId()).isEqualTo(workflowId);
        assertThat(test.getTaskType()).isEqualTo(taskType);
        assertThat(test.getUserId()).isEqualTo(userId);
        assertThat(test.getTaskStatus()).isEqualTo(taskStatus);
        assertThat(test.getErrorResponse()).isEqualTo(errorResponse);
        assertThat(test.getCompletedTime()).isEqualTo(completedTime);
        assertThat(test.getStartedTime()).isEqualTo(startedTime);
        assertThat(test.getRequestBody()).isEqualTo(mockRequestBody);
        assertThat(test.getResultProperties()).isEqualTo(mockResultProperties);
    }

    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(test.equals(o)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(o.hashCode());

        ServiceOrderDetails test2 = new ServiceOrderDetails();
        assertThat(test.equals(test2)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(test2.hashCode());

        BeanUtils.copyProperties(test, test2);
        assertThat(test.equals(test2)).isTrue();
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testToString() {
        String result =
                "ServiceOrderDetails(orderId="
                        + orderId
                        + ", serviceId="
                        + serviceId
                        + ", taskType="
                        + taskType
                        + ", taskStatus="
                        + taskStatus
                        + ", originalServiceId="
                        + originalServiceId
                        + ", parentOrderId="
                        + parentOrderId
                        + ", workflowId="
                        + workflowId
                        + ", errorResponse="
                        + errorResponse
                        + ", userId="
                        + userId
                        + ", startedTime="
                        + startedTime
                        + ", completedTime="
                        + completedTime
                        + ", requestBody="
                        + mockRequestBody
                        + ", resultProperties="
                        + mockResultProperties
                        + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
