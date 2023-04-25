/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor;

import java.util.Map;
import lombok.Data;
import org.eclipse.xpanse.modules.monitor.enums.MetricItemType;

/**
 * The one item of the metric.
 */
@Data
public class MetricItem {

    /**
     * The labels for the MetricItem.
     */
    public Map<String, String> labels;

    /**
     * The type of the MetricItem.
     */
    public MetricItemType type;

    /**
     * The value of the MetricItem.
     */
    public Number value;
}
