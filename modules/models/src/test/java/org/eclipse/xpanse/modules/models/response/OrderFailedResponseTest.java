package org.eclipse.xpanse.modules.models.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

class OrderFailedResponseTest {


    private final ResultType resultType = ResultType.ACCESS_DENIED;
    private final List<String> details = List.of(ResultType.ACCESS_DENIED.toValue());
    private final Boolean success = false;
    private final String id = UUID.randomUUID().toString();
    private OrderFailedResponse test;

    @BeforeEach
    void setUp() {
        test = OrderFailedResponse.errorResponse(resultType, details);
        test.setSuccess(success);
        test.setServiceId(id);
        test.setOrderId(id);
    }


    @Test
    void testGetters() {
        assertThat(test.getResultType()).isEqualTo(resultType);
        assertThat(test.getDetails()).isEqualTo(details);
        assertThat(test.getSuccess()).isEqualTo(success);
        assertThat(test.getServiceId()).isEqualTo(id);
        assertThat(test.getOrderId()).isEqualTo(id);
    }

    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(test.canEqual(o)).isFalse();
        assertThat(test.equals(o)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(o.hashCode());

        OrderFailedResponse test2 = OrderFailedResponse.errorResponse(resultType, details);
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
        String result = "OrderFailedResponse(serviceId=" + id + ", orderId=" + id + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
