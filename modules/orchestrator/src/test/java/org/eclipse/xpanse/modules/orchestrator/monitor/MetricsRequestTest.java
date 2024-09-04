package org.eclipse.xpanse.modules.orchestrator.monitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.UUID;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

class MetricsRequestTest {

    private final UUID serviceId = UUID.fromString("16e6e050-8933-4383-9e56-6131c8666d0c");
    private final String userId = "userId";
    @Mock
    private Region region;
    private MetricsRequest test;

    @BeforeEach
    void setUp() {
        test = new MetricsRequest(serviceId, region, MonitorResourceType.CPU, 0L, 0L, 1, false,
                userId);
    }

    @Test
    void testGetters() {
        assertEquals(MonitorResourceType.CPU, test.getMonitorResourceType());
        assertEquals(serviceId, test.getServiceId());
        assertEquals(region, test.getRegion());
        assertEquals(0L, test.getFrom());
        assertEquals(0L, test.getTo());
        assertEquals(1, test.getGranularity());
        assertFalse(test.isOnlyLastKnownMetric());
        assertEquals(userId, test.getUserId());
    }

    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(test.canEqual(o)).isFalse();
        assertThat(test).isNotEqualTo(o);
        assertThat(test.hashCode()).isNotEqualTo(o.hashCode());

        MetricsRequest test1 = new MetricsRequest(null, null, null, null, null, null, true, null);
        assertThat(test.canEqual(test1)).isTrue();
        assertThat(test).isNotEqualTo(test1);
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());

        BeanUtils.copyProperties(test, test1);
        assertThat(test.canEqual(test1)).isTrue();
        assertThat(test).isEqualTo(test1);
        assertThat(test.hashCode()).isEqualTo(test1.hashCode());
    }

    @Test
    void testToString() {
        assertNotEquals(test.toString(), null);
        String exceptedString = "MetricsRequest(serviceId=" + serviceId + ", region=" + region
                + ", monitorResourceType=" + MonitorResourceType.CPU
                + ", from=0, to=0, granularity=1,"
                + " onlyLastKnownMetric=false, userId=" + userId + ")";
        assertEquals(exceptedString, test.toString());
    }
}