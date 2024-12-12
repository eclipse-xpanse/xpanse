package org.eclipse.xpanse.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestStatus;
import org.junit.jupiter.api.Test;

class ServiceTemplateRequestStatusEnumConverterTest {

    private final ServiceTemplateChangeStatusEnumConverter converterTest =
            new ServiceTemplateChangeStatusEnumConverter();

    @Test
    void testConvert() throws Exception {
        assertThat(converterTest.convert("in-review"))
                .isEqualTo(ServiceTemplateRequestStatus.IN_REVIEW);
        assertThat(converterTest.convert("accepted"))
                .isEqualTo(ServiceTemplateRequestStatus.ACCEPTED);
        assertThat(converterTest.convert("rejected"))
                .isEqualTo(ServiceTemplateRequestStatus.REJECTED);
        assertThrows(UnsupportedEnumValueException.class, () -> converterTest.convert("unknown"));
    }
}
