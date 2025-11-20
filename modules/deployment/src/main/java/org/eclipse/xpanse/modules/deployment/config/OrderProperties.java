/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Properties class. */
@Data
@ConfigurationProperties(prefix = "xpanse.order")
public class OrderProperties {

    private OrderStatus orderStatus;
    private Integer maxOrderProcessingDurationSeconds;

    /** Properties class. */
    @Data
    public static class OrderStatus {
        private Integer longPollingSeconds;
        private Integer pollingIntervalSeconds;
    }
}
