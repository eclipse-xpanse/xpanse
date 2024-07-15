package org.eclipse.xpanse.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceStateManagementTaskType;
import org.junit.jupiter.api.Test;

class ManagementTaskTypeEnumConverterTest {

    private final ManagementTaskTypeEnumConverter converterTest =
            new ManagementTaskTypeEnumConverter();
    @Test
    void testConvert() {
        assertThat(converterTest.convert("start")).isEqualTo(ServiceStateManagementTaskType.START);
        assertThat(converterTest.convert("stop")).isEqualTo(ServiceStateManagementTaskType.STOP);
        assertThat(converterTest.convert("restart")).isEqualTo(ServiceStateManagementTaskType.RESTART);
        assertThrows(UnsupportedEnumValueException.class, () -> converterTest.convert("unknown"));
    }
}
