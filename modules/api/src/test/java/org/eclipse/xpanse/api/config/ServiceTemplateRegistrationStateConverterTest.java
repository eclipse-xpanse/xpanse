package org.eclipse.xpanse.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.junit.jupiter.api.Test;

class ServiceTemplateRegistrationStateConverterTest {

    private final ServiceTemplateRegistrationStateConverter converterTest =
            new ServiceTemplateRegistrationStateConverter();

    @Test
    void testConvert() {
        assertEquals(ServiceTemplateRegistrationState.IN_REVIEW,
                converterTest.convert("in-review"));
        assertEquals(ServiceTemplateRegistrationState.APPROVED, converterTest.convert("approved"));
        assertEquals(ServiceTemplateRegistrationState.REJECTED, converterTest.convert("rejected"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> converterTest.convert(" "));
        assertThrows(UnsupportedEnumValueException.class,
                () -> converterTest.convert("error_value"));
    }
}
