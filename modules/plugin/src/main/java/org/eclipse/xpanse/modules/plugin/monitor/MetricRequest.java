/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.plugin.monitor;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.xpanse.modules.plugin.monitor.enums.MonitorResourceType;

/**
 * The model to query metrics.
 */
@Data
@AllArgsConstructor
@SuppressWarnings("UnnecessarilyFullyQualified")
public class MetricRequest {

    private MonitorResourceType monitorResourceType;

    private Long from;

    private Long to;

    private Integer granularity;

    private boolean onlyLastKnownMetric;

    private String xpanseUserName;
}
