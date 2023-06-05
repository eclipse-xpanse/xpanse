/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.models;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.xpanse.modules.monitor.Metric;

/**
 * FlexibleEngine Metric.
 */

public class FlexibleEngineMetric {

    @Getter
    private final long time = System.currentTimeMillis();

    @Setter
    @Getter
    private Metric metric;

    public FlexibleEngineMetric(Metric metric) {
        this.metric = metric;
    }

}
