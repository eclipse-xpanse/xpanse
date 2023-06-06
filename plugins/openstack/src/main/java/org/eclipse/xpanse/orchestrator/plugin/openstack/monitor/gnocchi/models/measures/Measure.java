/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.models.measures;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data model for Measure objects. Measure in Gnocchi are returned as tuples <a href="https://gnocchi.osci.io/rest.html#id1">Tuples</a>.
 */
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public class Measure {

    private String timestamp;
    private String granularity;
    private Number value;

    /**
     * Constructor to deserialize a tuple to a Java object.
     *
     * @param timestamp   time when the measure was recorded.
     * @param granularity time difference between two measures.
     * @param value       actual value measures.
     */
    @JsonCreator
    public Measure(@JsonProperty("timestamp") String timestamp,
                   @JsonProperty("granularity") String granularity,
                   @JsonProperty("value") Number value) {
        this.timestamp = timestamp;
        this.value = value;
        this.granularity = granularity;
    }

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("granularity")
    public String getGranularity() {
        return granularity;
    }

    @JsonProperty("granularity")
    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    @JsonProperty("value")
    public Number getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(Number value) {
        this.value = value;
    }
}
