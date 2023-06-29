package org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.models.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetricsFilterTest {

    private MetricsFilter test;

    @BeforeEach
    void setUp() {
        test = new MetricsFilter(1L, 1L, 1);
    }

    @Test
    void testGetters() {
        assertEquals(1L, test.getStart());
        assertEquals(1L, test.getEnd());
        assertEquals(1, test.getGranularity());
    }


    @Test
    void testEqualsAndHashCode() {

        assertEquals(test, test);
        assertNotEquals(test.hashCode(), 0);

        Object object = new Object();
        assertNotEquals(test, object);
        assertNotEquals(test.hashCode(), object.hashCode());

        MetricsFilter test1 = new MetricsFilter(null, null, null);
        assertNotEquals(test, test1);
        assertNotEquals(test.hashCode(), test1.hashCode());

        MetricsFilter test2 = new MetricsFilter(null, null, null);
        MetricsFilter test3 = new MetricsFilter(null, null, null);
        test2.setStart(1L);
        test3.setStart(2L);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());

        test2.setEnd(1L);
        test3.setEnd(2L);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());

        test2.setGranularity(1);
        test3.setGranularity(300);
        assertNotEquals(test, test1);
        assertEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());
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
