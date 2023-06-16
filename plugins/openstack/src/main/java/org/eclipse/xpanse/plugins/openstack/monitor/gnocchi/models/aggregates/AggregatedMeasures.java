/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.models.aggregates;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Data model for AggregatedMeasures objects.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "measures"
})
public class AggregatedMeasures {

    @JsonProperty("measures")
    private AggregateMeasure aggregateMeasure;

    @JsonProperty("measures")
    public AggregateMeasure getMeasures() {
        return aggregateMeasure;
    }

    @JsonProperty("measures")
    public void setMeasures(AggregateMeasure measures) {
        this.aggregateMeasure = measures;
    }

}
