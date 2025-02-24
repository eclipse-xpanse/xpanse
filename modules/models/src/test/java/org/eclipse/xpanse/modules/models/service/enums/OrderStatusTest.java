package org.eclipse.xpanse.modules.models.service.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class OrderStatusTest {

    @Test
    void testGetByValue() {
        assertThat(OrderStatus.getByValue("created")).isEqualTo(OrderStatus.CREATED);
        assertThat(OrderStatus.getByValue("in-progress")).isEqualTo(OrderStatus.IN_PROGRESS);
        assertThat(OrderStatus.getByValue("successful")).isEqualTo(OrderStatus.SUCCESSFUL);
        assertThat(OrderStatus.getByValue("failed")).isEqualTo(OrderStatus.FAILED);
        assertThrows(UnsupportedEnumValueException.class, () -> OrderStatus.getByValue("unknown"));
    }

    @Test
    void testToValue() {
        assertThat(OrderStatus.CREATED.toValue()).isEqualTo("created");
        assertThat(OrderStatus.IN_PROGRESS.toValue()).isEqualTo("in-progress");
        assertThat(OrderStatus.SUCCESSFUL.toValue()).isEqualTo("successful");
        assertThat(OrderStatus.FAILED.toValue()).isEqualTo("failed");
    }
}
