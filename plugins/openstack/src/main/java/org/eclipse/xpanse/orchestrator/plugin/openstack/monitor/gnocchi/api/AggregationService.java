/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.api;

import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.models.aggregates.AggregatedMeasures;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.models.aggregates.AggregationRequest;
import org.springframework.stereotype.Component;

/**
 * Class to handle all Gnocchi aggregation services - <a href="https://gnocchi.osci.io/rest.html#dynamic-aggregates">Aggregation</a>.
 */
@Component
public class AggregationService extends BaseGnocchiServices {

    public AggregatedMeasures getAggregatedMeasuresByOperation(
            AggregationRequest aggregationRequest) {
        return post(AggregatedMeasures.class, "/v1/aggregates").entity(aggregationRequest)
                .execute();
    }
}
