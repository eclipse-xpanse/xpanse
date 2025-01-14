/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.monitor;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.models.service.deployment.DeployResource;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;

/** The model to query metrics for resource instance. */
@Getter
@EqualsAndHashCode(callSuper = true)
public class ServiceMetricsRequest extends MetricsRequest {

    @NotNull private final List<DeployResource> deployResources;

    /** Constructor to create ServiceMetricsRequest. */
    public ServiceMetricsRequest(
            UUID serviceId,
            Region region,
            List<DeployResource> deployResources,
            MonitorResourceType monitorType,
            Long from,
            Long to,
            Integer period,
            boolean onlyLastKnownMetric,
            String userId) {
        super(serviceId, region, monitorType, from, to, period, onlyLastKnownMetric, userId);
        this.deployResources = deployResources;
    }
}
