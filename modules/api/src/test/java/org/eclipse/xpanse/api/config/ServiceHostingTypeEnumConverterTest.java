package org.eclipse.xpanse.api.config;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ServiceHostingTypeEnumConverterTest {

    private final ServiceHostingTypeEnumConverter converterTest =
            new ServiceHostingTypeEnumConverter();

    @Test
    void testConvert() {
        Assertions.assertEquals(ServiceHostingType.SELF, converterTest.convert("self"));
        Assertions.assertEquals(
                ServiceHostingType.SERVICE_VENDOR, converterTest.convert("service-vendor"));
        Assertions.assertThrows(
                UnsupportedEnumValueException.class, () -> converterTest.convert("error_value"));
    }
}
