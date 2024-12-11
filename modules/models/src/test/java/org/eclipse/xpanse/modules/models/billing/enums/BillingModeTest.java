package org.eclipse.xpanse.modules.models.billing.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class BillingModeTest {

    @Test
    void testGetByValue() {
        assertThat(BillingMode.getByValue(BillingMode.FIXED.toValue()))
                .isEqualTo(BillingMode.FIXED);
        assertThat(BillingMode.getByValue(BillingMode.PAY_PER_USE.toValue()))
                .isEqualTo(BillingMode.PAY_PER_USE);
        assertThrows(
                UnsupportedEnumValueException.class, () -> BillingMode.getByValue("error_value"));
    }

    @Test
    void testToValue() {
        assertThat(BillingMode.FIXED.toValue()).isEqualTo(BillingMode.FIXED.toValue());
        assertThat(BillingMode.PAY_PER_USE.toValue()).isEqualTo(BillingMode.PAY_PER_USE.toValue());
    }
}
