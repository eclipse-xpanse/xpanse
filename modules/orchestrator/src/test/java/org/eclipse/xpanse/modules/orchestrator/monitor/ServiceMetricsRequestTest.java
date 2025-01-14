package org.eclipse.xpanse.modules.orchestrator.monitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class ServiceMetricsRequestTest {

    private final String userId = "userId";
    private final UUID serviceId = UUID.fromString("16e6e050-8933-4383-e56-6131c8666d0c");
    private final DeployResource deployResource = new DeployResource();
    private final List<DeployResource> deployResources = List.of(deployResource);
    @Mock private Region region;
    private ServiceMetricsRequest test;

    @BeforeEach
    void setUp() {
        test =
                new ServiceMetricsRequest(
                        serviceId,
                        region,
                        deployResources,
                        MonitorResourceType.CPU,
                        0L,
                        0L,
                        1,
                        false,
                        userId);
    }

    @Test
    void testGetters() {
        assertEquals(MonitorResourceType.CPU, test.getMonitorResourceType());
        assertEquals(serviceId, test.getServiceId());
        assertEquals(region, test.getRegion());
        assertEquals(deployResources, test.getDeployResources());
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

        ServiceMetricsRequest test1 =
                new ServiceMetricsRequest(null, null, null, null, null, null, null, false, null);
        assertThat(test.canEqual(test1)).isTrue();
        assertThat(test).isNotEqualTo(test1);
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());
    }

    @Test
    void testToString() {
        assertNotEquals(test.toString(), null);
        String exceptedString =
                "MetricsRequest(serviceId="
                        + serviceId
                        + ", region="
                        + region
                        + ", monitorResourceType="
                        + MonitorResourceType.CPU
                        + ", from=0, to=0, granularity=1,"
                        + " onlyLastKnownMetric=false, userId="
                        + userId
                        + ")";
        assertEquals(exceptedString, test.toString());
    }
}
