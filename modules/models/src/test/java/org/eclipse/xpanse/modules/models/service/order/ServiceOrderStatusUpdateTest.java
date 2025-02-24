package org.eclipse.xpanse.modules.models.service.order;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.service.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class ServiceOrderStatusUpdateTest {

    private final Boolean isOrderCompleted = false;
    private final OrderStatus orderStatus = OrderStatus.CREATED;

    private final ErrorResponse error = new ErrorResponse();

    private ServiceOrderStatusUpdate test;

    @BeforeEach
    void setUp() {
        test = new ServiceOrderStatusUpdate(orderStatus, isOrderCompleted, error);
    }

    @Test
    void testGetters() {
        assertThat(test.getOrderStatus()).isEqualTo(orderStatus);
        assertThat(test.getIsOrderCompleted()).isEqualTo(isOrderCompleted);
        assertThat(test.getError()).isEqualTo(error);
    }

    @Test
    void testHashCodeAndEquals() {
        Object o = new Object();
        assertThat(test.equals(o)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(o.hashCode());

        ServiceOrderStatusUpdate test1 = new ServiceOrderStatusUpdate(orderStatus, false, null);
        assertThat(test.equals(test1)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());

        BeanUtils.copyProperties(test, test1);
        assertThat(test.equals(test1)).isTrue();
        assertThat(test.hashCode()).isEqualTo(test1.hashCode());
    }

    @Test
    void testToString() {
        String expected =
                "ServiceOrderStatusUpdate(orderStatus="
                        + orderStatus
                        + ", "
                        + "isOrderCompleted="
                        + isOrderCompleted
                        + ", "
                        + "error="
                        + error
                        + ")";
        assertThat(test.toString()).isEqualTo(expected);
    }
}
