package org.eclipse.xpanse.modules.models.service.modify;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class ServiceModificationAuditDetailsTest {
    private final OffsetDateTime startedTime = OffsetDateTime.of(LocalDateTime.now(),
            ZoneOffset.UTC);
    private final OffsetDateTime completedTime = OffsetDateTime.of(LocalDateTime.now(),
            ZoneOffset.UTC);
    private final UUID serviceId = UUID.fromString("168b2be0-3535-4042-b53c-23cabd874a51");
    private final UUID id = UUID.fromString("4caabd86-1967-4351-aedc-b18cbab3ab61");
    private final String errorMsg = "error message";
    private final TaskStatus taskStatus = TaskStatus.SUCCESSFUL;
    @Mock
    private DeployRequest mockPreviousDeployRequest;
    @Mock
    private DeployRequest mockNewDeployRequest;
    @Mock
    private List<DeployResource> mockPreviousDeployedResources;
    @Mock
    private Map<String, String> mockPreviousDeployedResultProperties;
    @Mock
    private Map<String, String> mockPreviousDeployedServiceProperties;

    private ServiceModificationAuditDetails test;

    @BeforeEach
    void setUp() {
        test = new ServiceModificationAuditDetails();
        test.setId(id);
        test.setServiceId(serviceId);
        test.setStartedTime(startedTime);
        test.setCompletedTime(completedTime);
        test.setErrorMsg(errorMsg);
        test.setTaskStatus(taskStatus);
        test.setPreviousDeployRequest(mockPreviousDeployRequest);
        test.setNewDeployRequest(mockNewDeployRequest);
        test.setPreviousDeployedResources(mockPreviousDeployedResources);
        test.setPreviousDeployedResultProperties(mockPreviousDeployedResultProperties);
        test.setPreviousDeployedServiceProperties(mockPreviousDeployedServiceProperties);
    }

    @Test
    void testGetters() {
        assertThat(test.getId()).isEqualTo(id);
        assertThat(test.getServiceId()).isEqualTo(serviceId);
        assertThat(test.getTaskStatus()).isEqualTo(taskStatus);
        assertThat(test.getErrorMsg()).isEqualTo(errorMsg);
        assertThat(test.getCompletedTime()).isEqualTo(completedTime);
        assertThat(test.getStartedTime()).isEqualTo(startedTime);
        assertThat(test.getPreviousDeployRequest()).isEqualTo(mockPreviousDeployRequest);
        assertThat(test.getNewDeployRequest()).isEqualTo(mockNewDeployRequest);
        assertThat(test.getPreviousDeployedResources()).isEqualTo(mockPreviousDeployedResources);
        assertThat(test.getPreviousDeployedResultProperties())
                .isEqualTo(mockPreviousDeployedResultProperties);
        assertThat(test.getPreviousDeployedServiceProperties())
                .isEqualTo(mockPreviousDeployedServiceProperties);
    }

    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(test.canEqual(o)).isFalse();
        assertThat(test.equals(o)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(o.hashCode());

        ServiceModificationAuditDetails test2 = new ServiceModificationAuditDetails();
        assertThat(test.canEqual(test2)).isTrue();
        assertThat(test.equals(test2)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(test2.hashCode());

        BeanUtils.copyProperties(test, test2);
        assertThat(test.canEqual(test2)).isTrue();
        assertThat(test.equals(test2)).isTrue();
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testToString() {
        String result = "ServiceModificationAuditDetails(id=" + id + ", serviceId="
                + serviceId + ", taskStatus=" + taskStatus
                + ", errorMsg=" + errorMsg + ", startedTime=" + startedTime
                + ", completedTime=" + completedTime
                + ", previousDeployRequest=" + mockPreviousDeployRequest
                + ", newDeployRequest=" + mockNewDeployRequest
                + ", previousDeployedResources=" + mockPreviousDeployedResources
                + ", previousDeployedServiceProperties=" + mockPreviousDeployedServiceProperties
                + ", previousDeployedResultProperties=" + mockPreviousDeployedResultProperties
                + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
