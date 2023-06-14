/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.credential.CredentialVariables;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;

/**
 * The model to query metrics.
 */
@Data
@AllArgsConstructor
@SuppressWarnings("UnnecessarilyFullyQualified")
public class MetricRequest {

    @NotNull
    private AbstractCredentialInfo credential;

    private MonitorResourceType monitorResourceType;

    private Long from;

    private Long to;

    private Integer granularity;

    private boolean onlyLastKnownMetric;
}
