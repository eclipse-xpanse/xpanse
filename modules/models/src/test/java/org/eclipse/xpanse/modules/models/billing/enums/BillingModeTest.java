package org.eclipse.xpanse.modules.models.billing.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class BillingModeTest {

    @Test
    void testGetByValue() {
        assertThat(BillingMode.getByValue("fixed")).isEqualTo(BillingMode.FIXED);
        assertThat(BillingMode.getByValue("pay_per_use")).isEqualTo(BillingMode.PAY_PER_USE);
        assertThrows(UnsupportedEnumValueException.class,
                () -> BillingMode.getByValue("error_value"));
    }

    @Test
    void testToValue() {
        assertThat(BillingMode.FIXED.toValue()).isEqualTo("fixed");
        assertThat(BillingMode.PAY_PER_USE.toValue()).isEqualTo("pay_per_use");
    }
}
