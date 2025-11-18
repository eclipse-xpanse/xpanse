/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.cache.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.annotation.Validated;

/** Config class. */
@RefreshScope
@ConfigurationProperties(prefix = "xpanse.cache")
@Validated
@Data
public class CacheProperties {

    private boolean redisEnabled;

    @Min(1)
    private int availabilityZoneCacheMinutes = 60;

    @Min(1)
    private int servicePriceCacheMinutes = 60;

    @Min(1)
    private int monitorMetricsCacheMinutes = 60;
}
