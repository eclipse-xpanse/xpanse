package org.eclipse.xpanse.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestType;
import org.junit.jupiter.api.Test;

class ServiceTemplateRequestTypeEnumConverterTest {

    private final ServiceTemplateRequestTypeEnumConverter converterTest =
            new ServiceTemplateRequestTypeEnumConverter();

    @Test
    void testConvert() {
        assertThat(converterTest.convert("register"))
                .isEqualTo(ServiceTemplateRequestType.REGISTER);
        assertThat(converterTest.convert("update")).isEqualTo(ServiceTemplateRequestType.UPDATE);
        assertThat(converterTest.convert("unregister"))
                .isEqualTo(ServiceTemplateRequestType.UNREGISTER);
        assertThat(converterTest.convert("re-register"))
                .isEqualTo(ServiceTemplateRequestType.RE_REGISTER);
        assertThrows(UnsupportedEnumValueException.class, () -> converterTest.convert("unknown"));
    }
}
