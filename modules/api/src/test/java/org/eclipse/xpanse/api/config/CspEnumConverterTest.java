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
        assertEquals(converterTest.convert("huawei"), Csp.HUAWEI);
        assertEquals(converterTest.convert("flexibleEngine"), Csp.FLEXIBLE_ENGINE);
        assertEquals(converterTest.convert("openstack"), Csp.OPENSTACK);
        assertEquals(converterTest.convert("scs"), Csp.SCS);
        assertEquals(converterTest.convert("google"), Csp.GOOGLE);
        assertEquals(converterTest.convert("alicloud"), Csp.ALICLOUD);
        assertEquals(converterTest.convert("aws"), Csp.AWS);
        assertEquals(converterTest.convert("azure"), Csp.AZURE);
        Assertions.assertThrows(UnsupportedEnumValueException.class,
                () -> converterTest.convert("error_value"));
    }
}
