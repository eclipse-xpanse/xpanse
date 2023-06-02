/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.api;

import java.util.List;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.api.utils.MetricsQueryBuilder;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.models.filter.MetricsFilter;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.models.measures.Measure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class to handle all Gnocchi measures services -
 * <a href="https://gnocchi.osci.io/rest.html#measures">Measures</a>.
 */
@Component
public class MeasuresService extends BaseGnocchiServices {

    private final MetricsQueryBuilder metricsQueryBuilder;

    @Autowired
    public MeasuresService(MetricsQueryBuilder metricsQueryBuilder) {
        this.metricsQueryBuilder = metricsQueryBuilder;
    }

    /**
     * Get measurements recorded for a specific metric ID.
     *
     * @param resourceMetricId metric ID for the resource.
     * @return returns all the recorded measures for the requested metric of the resource.
     */
    public List<Measure> getMeasurementsForResourceByMetricId(String resourceMetricId,
                                                              MetricsFilter metricsFilter) {
        String requestUri = "/v1/metric/%s/measures"
                + this.metricsQueryBuilder.build(metricsFilter);
        Measure[] meters =
                get(Measure[].class, uri(requestUri, resourceMetricId)).execute();
        return wrapList(meters);

    }
}
