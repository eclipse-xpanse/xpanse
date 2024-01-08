package org.eclipse.xpanse.modules.orchestrator.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServiceMetricsRequestTest {

    DeployResource deployResource = new DeployResource();
    private final UUID serviceId = UUID.fromString("16e6e050-8933-4383-9e56-6131c8666d0c");
    private ServiceMetricsRequest test;

    @BeforeEach
    void setUp() {

        test = new ServiceMetricsRequest(serviceId, List.of(deployResource), MonitorResourceType.CPU, 0L, 0L,
                1,
                false,
                "userId");
    }

    @Test
    void testGetters() {
        assertEquals(MonitorResourceType.CPU, test.getMonitorResourceType());
        assertEquals(0L, test.getFrom());
        assertEquals(0L, test.getTo());
        assertEquals(1, test.getGranularity());
        assertFalse(test.isOnlyLastKnownMetric());
        assertEquals("userId", test.getUserId());
        assertEquals(deployResource, test.getDeployResources().get(0));
    }

    @Test
    void testEqualsAndHashCode() {
        assertNotEquals(test.hashCode(), 0);

        Object object = new Object();
        assertNotEquals(test, object);
        assertNotEquals(test.hashCode(), object.hashCode());

        ServiceMetricsRequest test1 =
                new ServiceMetricsRequest(serviceId, List.of(deployResource), null, null, null, null, true,
                        null);
        ServiceMetricsRequest test2 =
                new ServiceMetricsRequest(serviceId, List.of(new DeployResource()), null, null, null, null,
                        true, null);
        ServiceMetricsRequest test3 =
                new ServiceMetricsRequest(null, null, null, null, null, null, false, null);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());

        test1.setMonitorResourceType(MonitorResourceType.CPU);
        test2.setMonitorResourceType(MonitorResourceType.MEM);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());

        test1.setFrom(0L);
        test2.setFrom(1L);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());

        test1.setTo(0L);
        test2.setTo(1L);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());

        test1.setGranularity(1);
        test2.setGranularity(300);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());

        test1.setOnlyLastKnownMetric(true);
        test2.setOnlyLastKnownMetric(false);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());

        test1.setUserId("userId");
        test2.setUserId("userId2");
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());
    }

    @Test
    void testToString() {
        assertNotEquals(test.toString(), null);

        String exceptedString = "MetricsRequest(serviceId=16e6e050-8933-4383-9e56-6131c8666d0c, monitorResourceType=CPU, from=0, "
                + "to=0, granularity=1, onlyLastKnownMetric=false, userId=userId)";
        assertEquals(exceptedString, test.toString());
    }
}
