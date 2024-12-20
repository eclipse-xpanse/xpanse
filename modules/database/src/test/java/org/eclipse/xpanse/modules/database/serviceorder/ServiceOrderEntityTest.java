package org.eclipse.xpanse.modules.database.serviceorder;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.Handler;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class ServiceOrderEntityTest {
    private final OffsetDateTime startedTime =
            OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
    private final OffsetDateTime completedTime =
            OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
    private final UUID uuid = UUID.fromString("4caabd86-1967-4351-aedc-b18cbab3ab61");
    private final ErrorResponse errorResponse = new ErrorResponse();
    private final String userId = "userId";
    private final TaskStatus taskStatus = TaskStatus.SUCCESSFUL;
    private final Handler handler = Handler.TERRAFORM_LOCAL;
    private final ServiceOrderType taskType = ServiceOrderType.DEPLOY;
    @Mock private DeployRequest mockPreviousDeployRequest;
    @Mock private List<DeployResource> mockPreviousDeployedResources;
    @Mock private Map<String, String> mockPreviousDeployedResultProperties;
    @Mock private Map<String, String> mockPreviousDeployedServiceProperties;
    @Mock private Object mockRequest;
    @Mock private ServiceDeploymentEntity mockServiceDeploymentEntity;
    private ServiceOrderEntity test;

    @BeforeEach
    void setUp() {
        test = new ServiceOrderEntity();
        test.setOrderId(uuid);
        test.setParentOrderId(uuid);
        test.setWorkflowId(uuid.toString());
        test.setServiceDeploymentEntity(mockServiceDeploymentEntity);
        test.setOriginalServiceId(uuid);
        test.setTaskType(taskType);
        test.setUserId(userId);
        test.setStartedTime(startedTime);
        test.setCompletedTime(completedTime);
        test.setErrorResponse(errorResponse);
        test.setTaskStatus(taskStatus);
        test.setPreviousDeployRequest(mockPreviousDeployRequest);
        test.setRequestBody(mockRequest);
        test.setPreviousDeployedResources(mockPreviousDeployedResources);
        test.setPreviousDeployedResultProperties(mockPreviousDeployedResultProperties);
        test.setPreviousDeployedServiceProperties(mockPreviousDeployedServiceProperties);
        test.setHandler(handler);
    }

    @Test
    void testGetters() {
        assertThat(test.getOrderId()).isEqualTo(uuid);
        assertThat(test.getParentOrderId()).isEqualTo(uuid);
        assertThat(test.getServiceDeploymentEntity()).isEqualTo(mockServiceDeploymentEntity);
        assertThat(test.getOriginalServiceId()).isEqualTo(uuid);
        assertThat(test.getWorkflowId()).isEqualTo(uuid.toString());
        assertThat(test.getTaskType()).isEqualTo(taskType);
        assertThat(test.getUserId()).isEqualTo(userId);
        assertThat(test.getTaskStatus()).isEqualTo(taskStatus);
        assertThat(test.getErrorResponse()).isEqualTo(errorResponse);
        assertThat(test.getCompletedTime()).isEqualTo(completedTime);
        assertThat(test.getStartedTime()).isEqualTo(startedTime);
        assertThat(test.getPreviousDeployRequest()).isEqualTo(mockPreviousDeployRequest);
        assertThat(test.getPreviousDeployedResources()).isEqualTo(mockPreviousDeployedResources);
        assertThat(test.getPreviousDeployedResultProperties())
                .isEqualTo(mockPreviousDeployedResultProperties);
        assertThat(test.getPreviousDeployedServiceProperties())
                .isEqualTo(mockPreviousDeployedServiceProperties);
        assertThat(test.getRequestBody()).isEqualTo(mockRequest);
        assertThat(test.getHandler()).isEqualTo(handler);
    }

    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(test.equals(o)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(o.hashCode());

        ServiceOrderEntity test2 = new ServiceOrderEntity();
        assertThat(test.equals(test2)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(test2.hashCode());

        BeanUtils.copyProperties(test, test2);
        assertThat(test.equals(test2)).isTrue();
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testToString() {
        String result =
                "ServiceOrderEntity(orderId="
                        + uuid
                        + ", serviceDeploymentEntity="
                        + mockServiceDeploymentEntity
                        + ", parentOrderId="
                        + uuid
                        + ", workflowId="
                        + uuid
                        + ", originalServiceId="
                        + uuid
                        + ", taskType="
                        + taskType
                        + ", userId="
                        + userId
                        + ", taskStatus="
                        + taskStatus
                        + ", errorResponse="
                        + errorResponse
                        + ", startedTime="
                        + startedTime
                        + ", completedTime="
                        + completedTime
                        + ", previousDeployRequest="
                        + mockPreviousDeployRequest
                        + ", previousDeployedResources="
                        + mockPreviousDeployedResources
                        + ", previousDeployedServiceProperties="
                        + mockPreviousDeployedServiceProperties
                        + ", previousDeployedResultProperties="
                        + mockPreviousDeployedResultProperties
                        + ", requestBody="
                        + mockRequest
                        + ", handler="
                        + handler
                        + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
