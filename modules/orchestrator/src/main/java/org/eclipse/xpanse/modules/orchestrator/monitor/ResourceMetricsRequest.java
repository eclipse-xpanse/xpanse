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

/**
 * The model to query metrics for resource instance.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class ResourceMetricsRequest extends MetricsRequest {

    @NotNull
    private final DeployResource deployResource;

    public ResourceMetricsRequest(
            UUID serviceId,
            DeployResource deployResource,
            MonitorResourceType monitorResourceType,
            Long from,
            Long to,
            Integer period,
            boolean onlyLastKnownMetric,
            String userId) {
        super(serviceId, monitorResourceType, from, to, period, onlyLastKnownMetric, userId);
        this.deployResource = deployResource;
    }
}
