/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.eclipse.xpanse.modules.credential.CredentialDefinition;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;

/**
 * The model to query metrics for resource instance.
 */
@EqualsAndHashCode(callSuper = true)
public class ServiceMetricRequest extends MetricRequest {

    @NotNull
    @Getter
    private final List<DeployResource> deployResources;

    public ServiceMetricRequest(
            List<DeployResource> deployResources,
            @NotNull CredentialDefinition credential,
            MonitorResourceType monitorResourceType,
            Long from, Long to, Integer period) {
        super(credential, monitorResourceType, from, to, period);
        this.deployResources = deployResources;
    }
}
