package org.eclipse.xpanse.modules.monitor.cache;

import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ServiceMetricsCacheKeyTest {

    private final ServiceMetricsCacheKey testCacheKey =
            new ServiceMetricsCacheKey(Csp.HUAWEI, "resourceId", MonitorResourceType.CPU);

    @Test
    void testEquals() {
        // Setup

        final ServiceMetricsCacheKey cacheKey1 =
                new ServiceMetricsCacheKey(Csp.HUAWEI, "resourceId", MonitorResourceType.CPU);
        final ServiceMetricsCacheKey cacheKey2 =
                new ServiceMetricsCacheKey(Csp.HUAWEI, "resourceId1", MonitorResourceType.CPU);
        final ServiceMetricsCacheKey cacheKey3 =
                new ServiceMetricsCacheKey(null, null, null);
        final Object object = new Object();

        // Run the test
        final boolean result = testCacheKey.equals(object);
        final boolean result1 = testCacheKey.equals(cacheKey1);
        final boolean result2 = testCacheKey.equals(cacheKey2);
        final boolean result3 = testCacheKey.equals(cacheKey3);

        // Verify the results
        Assertions.assertFalse(result);
        Assertions.assertTrue(result1);
        Assertions.assertFalse(result2);
        Assertions.assertFalse(result3);
    }

    @Test
    void testHashCode() {
        // Setup
        final ServiceMetricsCacheKey cacheKey1 =
                new ServiceMetricsCacheKey(Csp.HUAWEI, "resourceId", MonitorResourceType.CPU);
        final ServiceMetricsCacheKey cacheKey2 =
                new ServiceMetricsCacheKey(Csp.HUAWEI, "resourceId1", MonitorResourceType.CPU);
        final ServiceMetricsCacheKey cacheKey3 =
                new ServiceMetricsCacheKey(null, null, null);
        // Run the test
        final int result = testCacheKey.hashCode();
        final int result1 = cacheKey1.hashCode();
        final int result2 = cacheKey2.hashCode();
        final int result3 = cacheKey3.hashCode();

        // Verify the results
        Assertions.assertEquals(result, result1);
        Assertions.assertNotEquals(result, result2);
        Assertions.assertNotEquals(result, result3);
        Assertions.assertNotEquals(result2, result3);
    }
}
