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
        assertThat(converterTest.convert("redeploy")).isEqualTo(ServiceOrderType.REDEPLOY);
        assertThat(converterTest.convert("destroy")).isEqualTo(ServiceOrderType.DESTROY);
        assertThat(converterTest.convert("purge")).isEqualTo(ServiceOrderType.PURGE);
        assertThrows(UnsupportedEnumValueException.class, () -> converterTest.convert("unknown"));
    }
}
