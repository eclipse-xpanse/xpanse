/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor;

import java.util.List;
import java.util.Map;
import lombok.Data;
import org.eclipse.xpanse.modules.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.monitor.enums.MetricUnit;

/**
 * One kind of metric.
 */
@Data
public class Metric {

    /**
     * The name of the metric.
     */
    String name;

    /**
     * The description of the metric.
     */
    String description;

    /**
     * The type of the metric.
     */
    MetricType type;

    /**
     * The type of the metric.
     */
    MetricUnit unit;

    /**
     * The labels of the metric.
     */
    Map<String, String> labels;

    /**
     * The list of the metric items.
     */
    List<MetricItem> metrics;

}
