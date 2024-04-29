package org.eclipse.xpanse.modules.models.billing.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class CurrencyTest {

    @Test
    void testGetByValue() {
        assertThat(Currency.getByValue("usd")).isEqualTo(Currency.USD);
        assertThat(Currency.getByValue("EUR")).isEqualTo(Currency.EUR);
        assertThat(Currency.getByValue("CNY")).isEqualTo(Currency.CNY);
        assertThrows(UnsupportedEnumValueException.class, () -> Currency.getByValue("null"));
    }

    @Test
    void testToValue() {
        assertThat(Currency.USD.toValue()).isEqualTo("USD");
        assertThat(Currency.EUR.toValue()).isEqualTo("EUR");
        assertThat(Currency.CNY.toValue()).isEqualTo("CNY");
    }
}
