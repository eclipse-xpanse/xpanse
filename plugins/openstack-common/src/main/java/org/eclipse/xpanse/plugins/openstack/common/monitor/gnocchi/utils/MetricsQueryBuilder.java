/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.utils;

import java.util.Objects;
import org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.models.filter.MetricsFilter;
import org.springframework.stereotype.Component;

/** Bean to handle query builder logic for metrics filter. */
@Component
public class MetricsQueryBuilder {

    /**
     * Creates URI query parameters string based on MetricsFilter object.
     *
     * @param metricsFilter MetricsFilter bean.
     * @return returns URI with query parameters.
     */
    public String build(MetricsFilter metricsFilter) {
        StringBuilder stringBuilder = new StringBuilder();
        if (Objects.nonNull(metricsFilter.getStart())) {
            stringBuilder.append("start=").append(metricsFilter.getStart());
        }
        if (Objects.nonNull(metricsFilter.getEnd())) {
            if (!stringBuilder.isEmpty()) {
                stringBuilder.append("&");
            }
            stringBuilder.append("end=").append(metricsFilter.getEnd());
        }
        if (Objects.nonNull(metricsFilter.getGranularity())) {
            if (!stringBuilder.isEmpty()) {
                stringBuilder.append("&");
            }
            stringBuilder.append("granularity=").append(metricsFilter.getGranularity());
        }
        if (!stringBuilder.isEmpty()) {
            stringBuilder.insert(0, "?");
        }
        return stringBuilder.toString();
    }
}
