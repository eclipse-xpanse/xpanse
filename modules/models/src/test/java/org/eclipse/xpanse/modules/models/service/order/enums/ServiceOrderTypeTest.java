package org.eclipse.xpanse.modules.models.service.order.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class ServiceOrderTypeTest {

    @Test
    void testGetByValue() {
        assertThat(ServiceOrderType.getByValue("deploy")).isEqualTo(
                ServiceOrderType.DEPLOY);
        assertThat(ServiceOrderType.getByValue("modify")).isEqualTo(
                ServiceOrderType.MODIFY);
        assertThat(ServiceOrderType.getByValue("redeploy")).isEqualTo(
                ServiceOrderType.REDEPLOY);
        assertThat(ServiceOrderType.getByValue("destroy")).isEqualTo(
                ServiceOrderType.DESTROY);
        assertThat(ServiceOrderType.getByValue("purge")).isEqualTo(
                ServiceOrderType.PURGE);
        assertThrows(UnsupportedEnumValueException.class,
                () -> ServiceOrderType.getByValue("unknown"));

    }

    @Test
    void testToValue() {
        assertThat(ServiceOrderType.DEPLOY.toValue()).isEqualTo("deploy");
        assertThat(ServiceOrderType.MODIFY.toValue()).isEqualTo("modify");
        assertThat(ServiceOrderType.REDEPLOY.toValue()).isEqualTo("redeploy");
        assertThat(ServiceOrderType.DESTROY.toValue()).isEqualTo("destroy");
        assertThat(ServiceOrderType.PURGE.toValue()).isEqualTo("purge");
        assertThrows(UnsupportedEnumValueException.class,
                () -> ServiceOrderType.getByValue("unknown"));
    }
}
