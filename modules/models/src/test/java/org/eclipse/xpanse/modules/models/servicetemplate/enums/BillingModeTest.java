package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class BillingModeTest {

    @Test
    void testGetByValue() {
        assertThat(BillingMode.getByValue("yearly")).isEqualTo(BillingMode.YEARLY);
        assertThat(BillingMode.getByValue("monthly")).isEqualTo(BillingMode.MONTHLY);
        assertThat(BillingMode.getByValue("daily")).isEqualTo(BillingMode.DAILY);
        assertThat(BillingMode.getByValue("hourly")).isEqualTo(BillingMode.HOURLY);
        assertThat(BillingMode.getByValue("pay_per_use")).isEqualTo(BillingMode.PAY_PER_USE);
        assertThrows(UnsupportedEnumValueException.class,
                () -> BillingMode.getByValue("error_value"));
    }

    @Test
    void testToValue() {
        assertThat(BillingMode.YEARLY.toValue()).isEqualTo("yearly");
        assertThat(BillingMode.MONTHLY.toValue()).isEqualTo("monthly");
        assertThat(BillingMode.DAILY.toValue()).isEqualTo("daily");
        assertThat(BillingMode.HOURLY.toValue()).isEqualTo("hourly");
        assertThat(BillingMode.PAY_PER_USE.toValue()).isEqualTo("pay_per_use");
    }
}
