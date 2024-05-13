package org.eclipse.xpanse.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ManagementTaskStatus;
import org.junit.jupiter.api.Test;

class ManagementTaskStatusEnumConverterTest {

    private final ManagementTaskStatusEnumConverter converterTest =
            new ManagementTaskStatusEnumConverter();

    @Test
    void testConvert() {
        assertThat(converterTest.convert("created")).isEqualTo(ManagementTaskStatus.CREATED);
        assertThat(converterTest.convert("in progress")).isEqualTo(ManagementTaskStatus.IN_PROGRESS);
        assertThat(converterTest.convert("successful")).isEqualTo(ManagementTaskStatus.SUCCESSFUL);
        assertThat(converterTest.convert("failed")).isEqualTo(ManagementTaskStatus.FAILED);
        assertThrows(UnsupportedEnumValueException.class, () -> converterTest.convert("unknown"));
    }
}
