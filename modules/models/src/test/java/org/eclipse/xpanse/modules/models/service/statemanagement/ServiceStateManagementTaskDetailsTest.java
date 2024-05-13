package org.eclipse.xpanse.modules.models.service.statemanagement;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ManagementTaskStatus;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceStateManagementTaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class ServiceStateManagementTaskDetailsTest {

    private final OffsetDateTime startedTime = OffsetDateTime.of(LocalDateTime.now(),
            ZoneOffset.UTC);
    private final OffsetDateTime completedTime = OffsetDateTime.of(LocalDateTime.now(),
            ZoneOffset.UTC);
    private final UUID serviceId = UUID.fromString("168b2be0-3535-4042-b53c-23cabd874a51");
    private final UUID taskId = UUID.fromString("4caabd86-1967-4351-aedc-b18cbab3ab61");
    private final String errorMsg = "error message";
    private final ServiceStateManagementTaskType taskType = ServiceStateManagementTaskType.START;
    private final ManagementTaskStatus taskStatus = ManagementTaskStatus.SUCCESSFUL;

    private ServiceStateManagementTaskDetails test;

    @BeforeEach
    void setUp() {
        test = new ServiceStateManagementTaskDetails();
        test.setTaskId(taskId);
        test.setServiceId(serviceId);
        test.setTaskType(taskType);
        test.setTaskStatus(taskStatus);
        test.setStartedTime(startedTime);
        test.setCompletedTime(completedTime);
        test.setErrorMsg(errorMsg);
    }


    @Test
    void testGetters() {
        assertThat(test.getTaskId()).isEqualTo(taskId);
        assertThat(test.getServiceId()).isEqualTo(serviceId);
        assertThat(test.getTaskType()).isEqualTo(taskType);
        assertThat(test.getTaskStatus()).isEqualTo(taskStatus);
        assertThat(test.getErrorMsg()).isEqualTo(errorMsg);
        assertThat(test.getCompletedTime()).isEqualTo(completedTime);
        assertThat(test.getStartedTime()).isEqualTo(startedTime);
    }

    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(test.canEqual(o)).isFalse();
        assertThat(test.equals(o)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(o.hashCode());

        ServiceStateManagementTaskDetails test2 = new ServiceStateManagementTaskDetails();
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
        String result = "ServiceStateManagementTaskDetails(taskId=" + taskId + ", serviceId="
                + serviceId + ", taskType=" + taskType + ", taskStatus=" + taskStatus
                + ", errorMsg=" + errorMsg + ", startedTime=" + startedTime
                + ", completedTime=" + completedTime + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
