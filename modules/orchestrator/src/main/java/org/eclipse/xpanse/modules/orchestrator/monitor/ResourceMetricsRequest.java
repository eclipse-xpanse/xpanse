/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.monitor;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;

/** The model to query metrics for resource instance. */
@Getter
@EqualsAndHashCode(callSuper = true)
public class ResourceMetricsRequest extends MetricsRequest {

    @NotNull private final DeployResource deployResource;

    /** Constructor to create ResourceMetricsRequest. */
    public ResourceMetricsRequest(
            UUID serviceId,
            Region region,
            DeployResource deployResource,
            MonitorResourceType monitorType,
            Long from,
            Long to,
            Integer period,
            boolean onlyLastKnownMetric,
            String userId) {
        super(serviceId, region, monitorType, from, to, period, onlyLastKnownMetric, userId);
        this.deployResource = deployResource;
    }
}
