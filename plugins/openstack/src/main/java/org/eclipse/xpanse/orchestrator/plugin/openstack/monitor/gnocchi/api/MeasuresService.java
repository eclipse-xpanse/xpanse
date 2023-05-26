/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.api;

import java.util.List;
import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.models.measures.Measure;
import org.springframework.stereotype.Component;

/**
 * Class to handle all Gnocchi measures services -
 * <a href="https://gnocchi.osci.io/rest.html#measures">Measures</a>.
 */
@Component
public class MeasuresService extends BaseGnocchiServices {

    /**
     * Get measurements recorded for a specific metric ID.
     *
     * @param resourceMetricId metric ID for the resource.
     * @return returns all the recorded measures for the requested metric of the resource.
     */
    public List<Measure> getMeasurementsForResourceByMetricId(String resourceMetricId) {
        Measure[] meters =
                get(Measure[].class, uri("/v1/metric/%s/measures", resourceMetricId)).execute();
        return wrapList(meters);

    }
}
