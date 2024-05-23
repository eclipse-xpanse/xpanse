package org.eclipse.xpanse.modules.models.billing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class ServicePriceTest {

    @Mock
    private Price mockRecurringPrice;
    @Mock
    private Price mockOneTimePaymentPrice;

    private ServicePrice test;

    @BeforeEach
    void setUp() {
        test = new ServicePrice();
        test.setRecurringPrice(mockRecurringPrice);
        test.setOneTimePaymentPrice(mockOneTimePaymentPrice);
    }

    @Test
    void testGetters() {
        assertThat(test.getRecurringPrice()).isEqualTo(mockRecurringPrice);
        assertThat(test.getOneTimePaymentPrice()).isEqualTo(mockOneTimePaymentPrice);
    }

    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(test.canEqual(o)).isFalse();
        assertThat(test.canEqual(new ServicePrice())).isTrue();
        assertThat(test.equals(o)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(o.hashCode());

        ServicePrice test1 = new ServicePrice();
        assertThat(test.equals(test1)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());

        BeanUtils.copyProperties(test, test1);
        assertThat(test.equals(test1)).isTrue();
        assertThat(test.hashCode()).isEqualTo(test1.hashCode());
    }

    @Test
    void testToString() {
        String result = "ServicePrice(recurringPrice=" + mockRecurringPrice
                + ", oneTimePaymentPrice=" + mockOneTimePaymentPrice + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
