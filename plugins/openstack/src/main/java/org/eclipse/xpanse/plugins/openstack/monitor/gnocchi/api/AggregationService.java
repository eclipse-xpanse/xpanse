/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api;

import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api.utils.MetricsQueryBuilder;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.models.aggregates.AggregatedMeasures;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.models.aggregates.AggregationRequest;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.models.filter.MetricsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class to handle all Gnocchi aggregation services - <a href="https://gnocchi.osci.io/rest.html#dynamic-aggregates">Aggregation</a>.
 */
@Component
public class AggregationService extends BaseGnocchiServices {

    private final MetricsQueryBuilder metricsQueryBuilder;

    @Autowired
    public AggregationService(MetricsQueryBuilder metricsQueryBuilder) {
        this.metricsQueryBuilder = metricsQueryBuilder;
    }

    /**
     * Queries Gnocchi aggregates API based on the operation and filter provided.
     *
     * @param aggregationRequest AggregationRequest object.
     * @param metricsFilter MetricsFilter object
     * @return AggregatedMeasures
     */
    public AggregatedMeasures getAggregatedMeasuresByOperation(
            AggregationRequest aggregationRequest, MetricsFilter metricsFilter) {
        String requestUri =
                "/v1/aggregates" + this.metricsQueryBuilder.build(metricsFilter);
        return post(AggregatedMeasures.class, requestUri).entity(aggregationRequest)
                .execute();
    }
}
