/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.models.aggregates;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.models.measures.Measure;

/**
 * Data model for AggregatedMeasure objects.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "aggregated"
})
public class AggregateMeasure {

    @JsonProperty("aggregated")
    private List<Measure> aggregated = new ArrayList<>();

    @JsonProperty("aggregated")
    public List<Measure> getAggregated() {
        return aggregated;
    }

    @JsonProperty("aggregated")
    public void setAggregated(List<Measure> aggregated) {
        this.aggregated = aggregated;
    }

}
