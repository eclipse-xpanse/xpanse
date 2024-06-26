package org.eclipse.xpanse.plugins.openstack.common.aggregates.measures;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.models.measures.Measure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MeasureTest {

    String timestamp = "1670446035149";
    String granularity = "300";
    double number = 0.0;
    private Measure test;

    @BeforeEach
    void setUp() {
        test = new Measure(timestamp, granularity, number);
    }

    @Test
    void testSettersAndGetters() {
        test.setTimestamp(String.valueOf(System.currentTimeMillis()));
        assertNotEquals(timestamp, test.getTimestamp());
        test.setGranularity("1");
        assertNotEquals(granularity, test.getGranularity());
        test.setValue(0.10);
        assertNotEquals(number, test.getValue());
    }
}