package org.eclipse.xpanse.modules.models.billing;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class FlavorPriceResultTest {

    private final String flavorName = "flavorName";
    private final boolean isSuccessful = true;
    private final BillingMode billingMode = BillingMode.FIXED;
    private final String errorMessage = "errorMessage";


    @Mock
    private Price mockRecurringPrice;
    @Mock
    private Price mockOneTimePaymentPrice;

    private FlavorPriceResult test;

    @BeforeEach
    void setUp() {
        test = new FlavorPriceResult();
        test.setFlavorName(flavorName);
        test.setSuccessful(isSuccessful);
        test.setBillingMode(billingMode);
        test.setErrorMessage(errorMessage);
        test.setRecurringPrice(mockRecurringPrice);
        test.setOneTimePaymentPrice(mockOneTimePaymentPrice);
    }

    @Test
    void testGetters() {
        assertThat(test.getFlavorName()).isEqualTo(flavorName);
        assertThat(test.isSuccessful()).isEqualTo(isSuccessful);
        assertThat(test.getBillingMode()).isEqualTo(billingMode);
        assertThat(test.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(test.getRecurringPrice()).isEqualTo(mockRecurringPrice);
        assertThat(test.getOneTimePaymentPrice()).isEqualTo(mockOneTimePaymentPrice);
    }

    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(test.canEqual(o)).isFalse();
        assertThat(test.canEqual(new FlavorPriceResult())).isTrue();
        assertThat(test.equals(o)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(o.hashCode());

        FlavorPriceResult test1 = new FlavorPriceResult();
        assertThat(test.equals(test1)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());

        BeanUtils.copyProperties(test, test1);
        assertThat(test.equals(test1)).isTrue();
        assertThat(test.hashCode()).isEqualTo(test1.hashCode());
    }

    @Test
    void testToString() {
        String result = "FlavorPriceResult("
                + "flavorName=" + flavorName
                + ", billingMode=" + billingMode
                + ", recurringPrice=" + mockRecurringPrice
                + ", oneTimePaymentPrice=" + mockOneTimePaymentPrice
                + ", isSuccessful=" + isSuccessful
                + ", errorMessage=" + errorMessage
                + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
