package org.eclipse.xpanse.modules.models.service.statemanagement.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Test;

class ServiceStateTest {

    @Test
    void testGetByValue() {
        assertEquals(ServiceState.NOT_RUNNING, ServiceState.getByValue("not running"));
        assertEquals(ServiceState.RUNNING, ServiceState.getByValue("running"));
        assertEquals(ServiceState.STARTING, ServiceState.getByValue("starting"));
        assertEquals(ServiceState.STOPPING, ServiceState.getByValue("stopping"));
        assertEquals(ServiceState.STOPPED, ServiceState.getByValue("stopped"));
        assertEquals(ServiceState.RESTARTING, ServiceState.getByValue("restarting"));
        assertEquals(ServiceState.UNKNOWN, ServiceState.getByValue("unknown"));
        assertThrows(
                UnsupportedEnumValueException.class, () -> ServiceState.getByValue("error_value"));
    }

    @Test
    void testToValue() {
        assertEquals("not running", ServiceState.NOT_RUNNING.toValue());
        assertEquals("running", ServiceState.RUNNING.toValue());
        assertEquals("starting", ServiceState.STARTING.toValue());
        assertEquals("stopping", ServiceState.STOPPING.toValue());
        assertEquals("stopped", ServiceState.STOPPED.toValue());
        assertEquals("restarting", ServiceState.RESTARTING.toValue());
    }
}
