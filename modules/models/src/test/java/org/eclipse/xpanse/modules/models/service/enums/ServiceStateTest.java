package org.eclipse.xpanse.modules.models.service.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class ServiceStateTest {

    @Test
    void testGetByValue() {
        assertEquals(ServiceState.getByValue("not running"), ServiceState.NOT_RUNNING);
        assertEquals(ServiceState.getByValue("running"), ServiceState.RUNNING);
        assertEquals(ServiceState.getByValue("starting"), ServiceState.STARTING);
        assertEquals(ServiceState.getByValue("stopping"), ServiceState.STOPPING);
        assertEquals(ServiceState.getByValue("stopped"), ServiceState.STOPPED);
        assertEquals(ServiceState.getByValue("restarting"), ServiceState.RESTARTING);
        assertThrows(UnsupportedEnumValueException.class, () -> ServiceState.getByValue("unknown"));
    }

    @Test
    void testToValue() {
        assertEquals(ServiceState.NOT_RUNNING.toValue(), "not running");
        assertEquals(ServiceState.RUNNING.toValue(), "running");
        assertEquals(ServiceState.STARTING.toValue(), "starting");
        assertEquals(ServiceState.STOPPING.toValue(), "stopping");
        assertEquals(ServiceState.STOPPED.toValue(), "stopped");
        assertEquals(ServiceState.RESTARTING.toValue(), "restarting");
    }
}
