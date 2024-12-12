package org.eclipse.xpanse.modules.models.service.order.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class ServiceOrderTypeTest {

    @Test
    void testGetByValue() {
        assertThat(ServiceOrderType.getByValue("deploy")).isEqualTo(ServiceOrderType.DEPLOY);
        assertThat(ServiceOrderType.getByValue("modify")).isEqualTo(ServiceOrderType.MODIFY);
        assertThat(ServiceOrderType.getByValue("retry")).isEqualTo(ServiceOrderType.RETRY);
        assertThat(ServiceOrderType.getByValue("destroy")).isEqualTo(ServiceOrderType.DESTROY);
        assertThat(ServiceOrderType.getByValue("purge")).isEqualTo(ServiceOrderType.PURGE);
        assertThat(ServiceOrderType.getByValue("serviceStart"))
                .isEqualTo(ServiceOrderType.SERVICE_START);
        assertThat(ServiceOrderType.getByValue("serviceStop"))
                .isEqualTo(ServiceOrderType.SERVICE_STOP);
        assertThat(ServiceOrderType.getByValue("serviceRestart"))
                .isEqualTo(ServiceOrderType.SERVICE_RESTART);
        assertThat(ServiceOrderType.getByValue("lockChange"))
                .isEqualTo(ServiceOrderType.LOCK_CHANGE);
        assertThat(ServiceOrderType.getByValue("ConfigChange"))
                .isEqualTo(ServiceOrderType.CONFIG_CHANGE);
        assertThat(ServiceOrderType.getByValue("rollback")).isEqualTo(ServiceOrderType.ROLLBACK);
        assertThrows(
                UnsupportedEnumValueException.class, () -> ServiceOrderType.getByValue("unknown"));
    }

    @Test
    void testToValue() {
        assertThat(ServiceOrderType.DEPLOY.toValue()).isEqualTo("deploy");
        assertThat(ServiceOrderType.MODIFY.toValue()).isEqualTo("modify");
        assertThat(ServiceOrderType.RETRY.toValue()).isEqualTo("retry");
        assertThat(ServiceOrderType.DESTROY.toValue()).isEqualTo("destroy");
        assertThat(ServiceOrderType.PURGE.toValue()).isEqualTo("purge");
        assertThat(ServiceOrderType.SERVICE_START.toValue()).isEqualTo("serviceStart");
        assertThat(ServiceOrderType.SERVICE_STOP.toValue()).isEqualTo("serviceStop");
        assertThat(ServiceOrderType.SERVICE_RESTART.toValue()).isEqualTo("serviceRestart");
        assertThat(ServiceOrderType.LOCK_CHANGE.toValue()).isEqualTo("lockChange");
        assertThat(ServiceOrderType.CONFIG_CHANGE.toValue()).isEqualTo("configChange");
        assertThat(ServiceOrderType.ROLLBACK.toValue()).isEqualTo("rollback");
        assertThrows(
                UnsupportedEnumValueException.class, () -> ServiceOrderType.getByValue("unknown"));
    }
}
