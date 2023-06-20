/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.models.metrics;

/**
 * Metric types generated by Ceilometer.
 */
public enum CeilometerMetricType {

    CPU("cpu"),
    MEMORY_USAGE("memory.usage"),
    NETWORK_INCOMING("network.incoming.bytes"),
    NETWORK_OUTGOING("network.outgoing.bytes");

    private final String metricType;

    CeilometerMetricType(String metricType) {
        this.metricType = metricType;
    }

    public String toValue() {
        return this.metricType;
    }
}