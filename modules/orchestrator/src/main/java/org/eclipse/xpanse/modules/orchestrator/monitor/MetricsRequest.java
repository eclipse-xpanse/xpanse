/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.monitor;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;

/** The model to query metrics. */
@Data
@AllArgsConstructor
@SuppressWarnings("UnnecessarilyFullyQualified")
public class MetricsRequest {

    private UUID serviceId;

    private Region region;

    private MonitorResourceType monitorResourceType;

    private Long from;

    private Long to;

    private Integer granularity;

    private boolean onlyLastKnownMetric;

    private String userId;
}
