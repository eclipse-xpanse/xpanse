package org.eclipse.xpanse.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.junit.jupiter.api.Test;

class ServiceRegistrationStateConverterTest {

    private final ServiceRegistrationStateConverter converterTest =
            new ServiceRegistrationStateConverter();

    @Test
    void testConvert() {
        assertEquals(ServiceRegistrationState.APPROVAL_PENDING,
                converterTest.convert("approval pending"));
        assertEquals(ServiceRegistrationState.APPROVED, converterTest.convert("approved"));
        assertEquals(ServiceRegistrationState.REJECTED, converterTest.convert("rejected"));
        assertThrows(UnsupportedEnumValueException.class,
                () -> converterTest.convert(" "));
        assertThrows(UnsupportedEnumValueException.class,
                () -> converterTest.convert("error_value"));
    }
}
