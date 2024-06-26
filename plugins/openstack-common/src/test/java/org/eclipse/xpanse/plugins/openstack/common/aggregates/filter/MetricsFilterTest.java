package org.eclipse.xpanse.plugins.openstack.common.aggregates.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.models.filter.MetricsFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

class MetricsFilterTest {

    private MetricsFilter test;

    @BeforeEach
    void setUp() {
        test = MetricsFilter.builder().start(1L).end(1L).granularity(1).build();
    }

    @Test
    void testGetters() {
        assertEquals(1L, test.getStart());
        assertEquals(1L, test.getEnd());
        assertEquals(1, test.getGranularity());
    }


    @Test
    void testEqualsAndHashCode() {

        Object obj = new Object();
        assertNotEquals(test, obj);
        assertNotEquals(test.hashCode(), obj.hashCode());

        MetricsFilter test1 = MetricsFilter.builder().build();
        assertNotEquals(test, test1);
        assertNotEquals(test.hashCode(), test1.hashCode());

        BeanUtils.copyProperties(test, test1);
        assertEquals(test, test1);
        assertEquals(test.hashCode(), test1.hashCode());
    }


    @Test
    void testToString() {
        String exceptedString = "MetricsFilter(start=1, end=1, granularity=1)";
        assertEquals(test.toString(), exceptedString);
        assertNotEquals(test.toString(), null);
    }

    @Test
    void testBuilder() {

        String builderString = "MetricsFilter.MetricsFilterBuilder(start=1, end=1, granularity=1)";
        final MetricsFilter.MetricsFilterBuilder builder1 = MetricsFilter.builder();
        builder1.start(1L).end(1L).granularity(1);
        assertEquals(builder1.toString(), builderString);

        final MetricsFilter.MetricsFilterBuilder builder2 = MetricsFilter.builder();
        builder2.granularity(300);

        assertEquals(test, builder1.build());

        assertNotEquals(test, builder2.build());
    }
}
