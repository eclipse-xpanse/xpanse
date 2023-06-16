/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.monitor.models;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.xpanse.modules.plugin.monitor.Metric;

/**
 * Huawei Metric.
 */

public class HuaweiCloudMetric {

    @Getter
    private final long time = System.currentTimeMillis();

    @Setter
    @Getter
    private Metric metric;

    public HuaweiCloudMetric(Metric metric) {
        this.metric = metric;
    }

}
