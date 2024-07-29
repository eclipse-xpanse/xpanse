package org.eclipse.xpanse.modules.models.service.order;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ServiceOrderStatusUpdateTest {

    private final Boolean isOrderCompleted = false;
    private final TaskStatus taskStatus = TaskStatus.CREATED;

    private final String errorMsg = "errorMessage";

    private Map<String, String> deployedServiceProperties = new HashMap<>();

    private ServiceOrderStatusUpdate test;

    @BeforeEach
    void setUp() {
        test = new ServiceOrderStatusUpdate(taskStatus, isOrderCompleted, errorMsg, deployedServiceProperties);
    }


    @Test
    void testGetters() {
        assertThat(test.getTaskStatus()).isEqualTo(taskStatus);
        assertThat(test.getIsOrderCompleted()).isEqualTo(isOrderCompleted);
        assertThat(test.getErrorMsg()).isEqualTo(errorMsg);
        assertThat(test.getDeployedServiceProperties()).isEqualTo(deployedServiceProperties);
    }


    @Test
    void testHashCodeAndEquals() {
        Object o = new Object();
        assertThat(test.equals(o)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(o.hashCode());

        ServiceOrderStatusUpdate test1 = new ServiceOrderStatusUpdate(taskStatus, false, null,deployedServiceProperties);
        assertThat(test.equals(test1)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());

        BeanUtils.copyProperties(test, test1);
        assertThat(test.equals(test1)).isTrue();
        assertThat(test.hashCode()).isEqualTo(test1.hashCode());
    }

    @Test
    void testToString() {
        String expected = "ServiceOrderStatusUpdate(taskStatus=" + taskStatus + ", "
                + "isOrderCompleted=" + isOrderCompleted + ", "
                + "errorMsg=" + errorMsg + ", "
                + "deployedServiceProperties=" + deployedServiceProperties+")";
        assertThat(test.toString()).isEqualTo(expected);
    }
}
