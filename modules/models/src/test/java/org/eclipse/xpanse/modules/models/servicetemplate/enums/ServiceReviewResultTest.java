package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ServiceReviewResultTest {

    @Test
    void testGetByValue() {
        Assertions.assertEquals(
                ServiceReviewResult.APPROVED, ServiceReviewResult.getByValue("approved"));
        Assertions.assertEquals(
                ServiceReviewResult.REJECTED, ServiceReviewResult.getByValue("rejected"));

        assertThrows(
                UnsupportedEnumValueException.class,
                () -> ServiceReviewResult.getByValue("error_value"));
        assertThrows(
                UnsupportedEnumValueException.class, () -> ServiceReviewResult.getByValue(null));
    }

    @Test
    void testToValue() {
        Assertions.assertEquals("approved", ServiceReviewResult.APPROVED.toValue());
        Assertions.assertEquals("rejected", ServiceReviewResult.REJECTED.toValue());
    }
}
