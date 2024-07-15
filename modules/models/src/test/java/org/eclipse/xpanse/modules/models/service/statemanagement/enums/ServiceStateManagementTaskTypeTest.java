package org.eclipse.xpanse.modules.models.service.statemanagement.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class ServiceStateManagementTaskTypeTest {

    @Test
    void testGetByValue() {
        assertThat(ServiceStateManagementTaskType.getByValue("start")).isEqualTo(
                ServiceStateManagementTaskType.START);
        assertThat(ServiceStateManagementTaskType.getByValue("stop")).isEqualTo(
                ServiceStateManagementTaskType.STOP);
        assertThat(ServiceStateManagementTaskType.getByValue("restart")).isEqualTo(
                ServiceStateManagementTaskType.RESTART);
        assertThrows(UnsupportedEnumValueException.class,
                () -> ServiceStateManagementTaskType.getByValue("unknown"));
    }

    @Test
    void testToValue() {
        assertThat(ServiceStateManagementTaskType.START.toValue()).isEqualTo("start");
        assertThat(ServiceStateManagementTaskType.STOP.toValue()).isEqualTo("stop");
        assertThat(ServiceStateManagementTaskType.RESTART.toValue()).isEqualTo("restart");
    }
}
