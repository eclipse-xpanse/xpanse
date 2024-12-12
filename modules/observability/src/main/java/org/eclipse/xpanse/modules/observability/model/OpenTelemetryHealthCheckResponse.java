/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.observability.model;

import lombok.Data;

/**
 * Healthcheck response format returned from otel-collector health-check extension. See details in
 * <a
 * href="https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/extension/healthcheckextension#readme">health-check-extension</a>
 */
@Data
public class OpenTelemetryHealthCheckResponse {

    CollectorStatus status;
    String upSince;
    String uptime;
}
