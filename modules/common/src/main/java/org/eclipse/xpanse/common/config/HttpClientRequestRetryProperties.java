/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** properties class. */
@Data
@ConfigurationProperties(prefix = "xpanse.http-client-request")
public class HttpClientRequestRetryProperties {

    private Integer retryMaxAttempts;
    private Integer delayMilliseconds;
}
