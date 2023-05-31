/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.eclipse.xpanse.modules.credential.CredentialDefinition;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;

/**
 * The model to query metrics for resource instance.
 */
@EqualsAndHashCode(callSuper = true)
public class ResourceMetricRequest extends MetricRequest {

    @NotNull
    @Getter
    private final DeployResource deployResource;

    public ResourceMetricRequest(
            DeployResource deployResource,
            @NotNull CredentialDefinition credential,
            MonitorResourceType monitorResourceType,
            Long from, Long to, Integer period) {
        super(credential, monitorResourceType, from, to, period);
        this.deployResource = deployResource;
    }
}
