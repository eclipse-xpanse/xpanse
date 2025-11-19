/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.observability.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Properties class. */
@Data
@ConfigurationProperties(prefix = "otel")
public class OtelProperties {

    private Exporter exporter;
    private Sdk sdk;

    /** Properties class. */
    @Data
    public static class Exporter {
        private Otlp otlp;
        private Integer healthCheckPort;
    }

    /** Properties class. */
    @Data
    public static class Otlp {
        private String endpoint;
    }

    /** Properties class. */
    @Data
    public static class Sdk {
        private Boolean disabled;
    }
}
