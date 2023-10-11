package org.eclipse.xpanse.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MonitorResourceTypeEnumConverterTest {

    private MonitorResourceTypeEnumConverter monitorResourceTypeEnumConverterUnderTest;

    @BeforeEach
    void setUp() {
        monitorResourceTypeEnumConverterUnderTest = new MonitorResourceTypeEnumConverter();
    }

    @Test
    void testConvert() {
        assertThat(
                monitorResourceTypeEnumConverterUnderTest.convert("CPU"))
                .isEqualTo(MonitorResourceType.CPU);
        assertThrows(UnsupportedEnumValueException.class, () ->
                monitorResourceTypeEnumConverterUnderTest.convert("monitorResourceType"));
    }
}
