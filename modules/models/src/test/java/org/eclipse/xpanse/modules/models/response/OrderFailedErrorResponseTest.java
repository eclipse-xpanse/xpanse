package org.eclipse.xpanse.modules.models.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

class OrderFailedErrorResponseTest {


    private final ErrorType errorType = ErrorType.ACCESS_DENIED;
    private final List<String> details = List.of(ErrorType.ACCESS_DENIED.toValue());
    private final String id = UUID.randomUUID().toString();
    private OrderFailedErrorResponse test;

    @BeforeEach
    void setUp() {
        test = OrderFailedErrorResponse.errorResponse(errorType, details);
        test.setServiceId(id);
        test.setOrderId(id);
    }


    @Test
    void testGetters() {
        assertThat(test.getErrorType()).isEqualTo(errorType);
        assertThat(test.getDetails()).isEqualTo(details);
        assertThat(test.getServiceId()).isEqualTo(id);
        assertThat(test.getOrderId()).isEqualTo(id);
    }

    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(test.canEqual(o)).isFalse();
        assertThat(test.equals(o)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(o.hashCode());

        OrderFailedErrorResponse test2 = OrderFailedErrorResponse.errorResponse(errorType, details);
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
        String result = "OrderFailedErrorResponse(serviceId=" + id + ", orderId=" + id + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
