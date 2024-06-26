package org.eclipse.xpanse.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CspEnumConverterTest {

    private final CspEnumConverter converterTest = new CspEnumConverter();

    @Test
    void testConvert() {
        assertEquals(converterTest.convert("HuaweiCloud"), Csp.HUAWEI_CLOUD);
        assertEquals(converterTest.convert("FlexibleEngine"), Csp.FLEXIBLE_ENGINE);
        assertEquals(converterTest.convert("OpenstackTestlab"), Csp.OPENSTACK_TESTLAB);
        assertEquals(converterTest.convert("PlusServer"), Csp.PLUS_SERVER);
        assertEquals(converterTest.convert("GoogleCloudPlatform"), Csp.GCP);
        assertEquals(converterTest.convert("AlibabaCloud"), Csp.ALIBABA_CLOUD);
        assertEquals(converterTest.convert("aws"), Csp.AWS);
        assertEquals(converterTest.convert("azure"), Csp.AZURE);
        Assertions.assertThrows(UnsupportedEnumValueException.class,
                () -> converterTest.convert("error_value"));
    }
}
