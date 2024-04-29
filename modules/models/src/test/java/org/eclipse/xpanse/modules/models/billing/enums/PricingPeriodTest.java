package org.eclipse.xpanse.modules.models.billing.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class PricingPeriodTest {

    @Test
    void testGetByValue() {
        assertThat(PricingPeriod.getByValue("yearly")).isEqualTo(PricingPeriod.YEARLY);
        assertThat(PricingPeriod.getByValue("monthly")).isEqualTo(PricingPeriod.MONTHLY);
        assertThat(PricingPeriod.getByValue("daily")).isEqualTo(PricingPeriod.DAILY);
        assertThat(PricingPeriod.getByValue("hourly")).isEqualTo(PricingPeriod.HOURLY);
        assertThat(PricingPeriod.getByValue("oneTime")).isEqualTo(PricingPeriod.ONE_TIME);
        assertThrows(UnsupportedEnumValueException.class, () -> PricingPeriod.getByValue("null"));
    }

    @Test
    void testToValue() {
        assertThat(PricingPeriod.YEARLY.toValue()).isEqualTo("yearly");
        assertThat(PricingPeriod.MONTHLY.toValue()).isEqualTo("monthly");
        assertThat(PricingPeriod.DAILY.toValue()).isEqualTo("daily");
        assertThat(PricingPeriod.HOURLY.toValue()).isEqualTo("hourly");
        assertThat(PricingPeriod.ONE_TIME.toValue()).isEqualTo("oneTime");
    }
}
