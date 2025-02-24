package org.eclipse.xpanse.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.service.enums.OrderStatus;
import org.junit.jupiter.api.Test;

class OrderStatusEnumConverterTest {

    private final OrderStatusEnumConverter converterTest = new OrderStatusEnumConverter();

    @Test
    void testConvert() {
        assertThat(converterTest.convert("created")).isEqualTo(OrderStatus.CREATED);
        assertThat(converterTest.convert("in-progress")).isEqualTo(OrderStatus.IN_PROGRESS);
        assertThat(converterTest.convert("successful")).isEqualTo(OrderStatus.SUCCESSFUL);
        assertThat(converterTest.convert("failed")).isEqualTo(OrderStatus.FAILED);
        assertThrows(UnsupportedEnumValueException.class, () -> converterTest.convert("unknown"));
    }
}
