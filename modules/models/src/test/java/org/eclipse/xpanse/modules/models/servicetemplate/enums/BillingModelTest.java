package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class BillingModelTest {

    @Test
    void testGetByValue() {
        assertThat(BillingModel.getByValue("yearly")).isEqualTo(BillingModel.YEARLY);
        assertThat(BillingModel.getByValue("monthly")).isEqualTo(BillingModel.MONTHLY);
        assertThat(BillingModel.getByValue("daily")).isEqualTo(BillingModel.DAILY);
        assertThat(BillingModel.getByValue("hourly")).isEqualTo(BillingModel.HOURLY);
        assertThat(BillingModel.getByValue("pay_per_use")).isEqualTo(BillingModel.PAY_PER_USE);
        assertThrows(UnsupportedEnumValueException.class,
                () -> BillingModel.getByValue("error_value"));
    }

    @Test
    void testToValue() {
        assertThat(BillingModel.YEARLY.toValue()).isEqualTo("yearly");
        assertThat(BillingModel.MONTHLY.toValue()).isEqualTo("monthly");
        assertThat(BillingModel.DAILY.toValue()).isEqualTo("daily");
        assertThat(BillingModel.HOURLY.toValue()).isEqualTo("hourly");
        assertThat(BillingModel.PAY_PER_USE.toValue()).isEqualTo("pay_per_use");
    }
}
