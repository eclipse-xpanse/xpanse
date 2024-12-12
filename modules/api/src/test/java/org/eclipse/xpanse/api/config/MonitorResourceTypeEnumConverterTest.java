package org.eclipse.xpanse.api.config;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MonitorResourceTypeEnumConverterTest {

    private final MonitorResourceTypeEnumConverter converterTest =
            new MonitorResourceTypeEnumConverter();

    @Test
    void testConvert() {

        Assertions.assertEquals(MonitorResourceType.CPU, converterTest.convert("cpu"));
        Assertions.assertEquals(MonitorResourceType.MEM, converterTest.convert("mem"));
        Assertions.assertEquals(
                MonitorResourceType.VM_NETWORK_INCOMING,
                converterTest.convert("vm_network_incoming"));
        Assertions.assertEquals(
                MonitorResourceType.VM_NETWORK_OUTGOING,
                converterTest.convert("vm_network_outgoing"));
        Assertions.assertThrows(
                UnsupportedEnumValueException.class, () -> converterTest.convert("error_value"));
    }
}
