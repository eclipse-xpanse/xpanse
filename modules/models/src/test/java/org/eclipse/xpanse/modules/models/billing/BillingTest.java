package org.eclipse.xpanse.modules.models.billing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

public class BillingTest {

    private Billing billingTest;

    @BeforeEach
    void setUp() {
        billingTest = new Billing();
        billingTest.setBillingModes(Arrays.asList(BillingMode.FIXED, BillingMode.PAY_PER_USE));
        billingTest.setDefaultBillingMode(BillingMode.PAY_PER_USE);
    }

    @Test
    void testGetters() {
        assertThat(billingTest.getBillingModes()).isEqualTo(Arrays.asList(BillingMode.FIXED,
                BillingMode.PAY_PER_USE));
        assertThat(billingTest.getDefaultBillingMode()).isEqualTo(BillingMode.PAY_PER_USE);
    }

    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(billingTest.canEqual(o)).isFalse();
        assertThat(billingTest.canEqual(new Billing())).isTrue();
        assertThat(billingTest.equals(o)).isFalse();
        assertThat(billingTest.hashCode()).isNotEqualTo(o.hashCode());

        Billing billing1 = new Billing();
        assertThat(billingTest.equals(billing1)).isFalse();
        assertThat(billingTest.hashCode()).isNotEqualTo(billing1.hashCode());

        BeanUtils.copyProperties(billingTest, billing1);
        assertThat(billingTest.equals(billing1)).isTrue();
        assertThat(billingTest.hashCode()).isEqualTo(billing1.hashCode());
    }

    @Test
    void testToString() {
        String result =
                "Billing(billingModes=[FIXED, PAY_PER_USE], defaultBillingMode=PAY_PER_USE)";
        assertThat(billingTest.toString()).isEqualTo(result);
    }

}
