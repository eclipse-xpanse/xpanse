/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.observability;

import java.net.URI;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.eclipse.xpanse.modules.observability.config.OtelProperties;
import org.eclipse.xpanse.modules.observability.model.CollectorStatus;
import org.eclipse.xpanse.modules.observability.model.OpenTelemetryHealthCheckResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Bean to manage OpenTelemetry Collector Health check. */
@Component
@Slf4j
public class OpenTelemetryCollectorHealthCheck {

    private final OtelProperties otelProperties;

    @Autowired
    public OpenTelemetryCollectorHealthCheck(OtelProperties otelProperties) {
        this.otelProperties = otelProperties;
    }

    /**
     * Method checks the status of opentelemetry collector. Depends on health-check extension of
     * otel collector. See details in <a
     * href="https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/extension/healthcheckextension#readme">health-check-extension</a>
     *
     * @return BackendSystemStatus
     */
    public BackendSystemStatus getOpenTelemetryHealthStatus() {
        if (!otelProperties.getSdk().getDisabled()) {
            RestClient restClient = RestClient.builder().baseUrl(getHealthCheckUrl()).build();
            BackendSystemStatus backendSystemStatus = new BackendSystemStatus();
            backendSystemStatus.setName("Opentelemetry Collector");
            backendSystemStatus.setBackendSystemType(BackendSystemType.OPEN_TELEMETRY_COLLECTOR);
            backendSystemStatus.setEndpoint(
                    this.otelProperties.getExporter().getOtlp().getEndpoint());
            try {
                OpenTelemetryHealthCheckResponse openTelemetryHealthCheckResponse =
                        restClient.get().retrieve().body(OpenTelemetryHealthCheckResponse.class);
                backendSystemStatus.setHealthStatus(
                        Objects.nonNull(openTelemetryHealthCheckResponse)
                                        && openTelemetryHealthCheckResponse.getStatus()
                                                == CollectorStatus.SERVER_AVAILABLE
                                ? HealthStatus.OK
                                : HealthStatus.NOK);
            } catch (RestClientException restClientException) {
                backendSystemStatus.setHealthStatus(HealthStatus.NOK);
                backendSystemStatus.setDetails(restClientException.getMessage());
                log.error(restClientException.getMessage(), restClientException);
            }
            return backendSystemStatus;
        }
        return null;
    }

    private String getHealthCheckUrl() {
        URI url = URI.create(this.otelProperties.getExporter().getOtlp().getEndpoint());
        return url.getScheme()
                + "://"
                + url.getHost()
                + ":"
                + this.otelProperties.getExporter().getHealthCheckPort();
    }
}
