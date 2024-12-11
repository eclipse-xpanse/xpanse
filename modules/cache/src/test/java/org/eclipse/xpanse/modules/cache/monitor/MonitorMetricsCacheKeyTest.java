package org.eclipse.xpanse.modules.cache.monitor;

import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MonitorMetricsCacheKeyTest {

    private final MonitorMetricsCacheKey testCacheKey =
            new MonitorMetricsCacheKey(Csp.HUAWEI_CLOUD, "resourceId", MonitorResourceType.CPU);

    @Test
    void testEqualsAndHashCode() {
        final Object object = new Object();
        Assertions.assertNotEquals(testCacheKey, object);
        Assertions.assertNotEquals(testCacheKey.hashCode(), object.hashCode());

        final MonitorMetricsCacheKey cacheKey1 = new MonitorMetricsCacheKey(null, null, null);
        Assertions.assertNotEquals(testCacheKey, cacheKey1);
        Assertions.assertNotEquals(testCacheKey.hashCode(), cacheKey1.hashCode());

        final MonitorMetricsCacheKey cacheKey2 =
                new MonitorMetricsCacheKey(
                        Csp.HUAWEI_CLOUD, "resourceId1", MonitorResourceType.CPU);
        Assertions.assertNotEquals(testCacheKey, cacheKey2);
        Assertions.assertNotEquals(testCacheKey.hashCode(), cacheKey2.hashCode());

        final MonitorMetricsCacheKey cacheKey3 =
                new MonitorMetricsCacheKey(Csp.HUAWEI_CLOUD, "resourceId", MonitorResourceType.CPU);
        Assertions.assertEquals(testCacheKey, cacheKey3);
        Assertions.assertEquals(testCacheKey.hashCode(), cacheKey3.hashCode());
    }

    @Test
    void testToString() {
        String result =
                "MonitorMetricsCacheKey[csp=HUAWEI_CLOUD, monitorResourceId=resourceId, "
                        + "monitorResourceType=CPU]";
        Assertions.assertEquals(result, testCacheKey.toString());
    }
}
