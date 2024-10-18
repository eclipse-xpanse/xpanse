package org.eclipse.xpanse.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.junit.jupiter.api.Test;

class ServiceOrderTypeEnumConverterTest {

    private final ServiceOrderTypeEnumConverter converterTest =
            new ServiceOrderTypeEnumConverter();

    @Test
    void testConvert() {
        assertThat(converterTest.convert("deploy")).isEqualTo(ServiceOrderType.DEPLOY);
        assertThat(converterTest.convert("modify")).isEqualTo(ServiceOrderType.MODIFY);
        assertThat(converterTest.convert("retry")).isEqualTo(ServiceOrderType.RETRY);
        assertThat(converterTest.convert("destroy")).isEqualTo(ServiceOrderType.DESTROY);
        assertThat(converterTest.convert("purge")).isEqualTo(ServiceOrderType.PURGE);
        assertThat(converterTest.convert("rollback")).isEqualTo(ServiceOrderType.ROLLBACK);
        assertThat(converterTest.convert("lockChange")).isEqualTo(ServiceOrderType.LOCK_CHANGE);
        assertThat(converterTest.convert("configChange"))
                .isEqualTo(ServiceOrderType.CONFIG_CHANGE);
        assertThrows(UnsupportedEnumValueException.class, () -> converterTest.convert("unknown"));
    }
}
