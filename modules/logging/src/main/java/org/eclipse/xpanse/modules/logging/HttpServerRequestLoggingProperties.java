/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.logging;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/** properties class. */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "xpanse.http-server-request-logging")
public class HttpServerRequestLoggingProperties {

    private Boolean enabled;
    private List<String> excludeUri;
}
