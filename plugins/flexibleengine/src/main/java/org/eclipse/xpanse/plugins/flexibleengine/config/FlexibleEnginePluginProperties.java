/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.flexibleengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/** properties class. */
@RefreshScope
@Data
@ConfigurationProperties(prefix = "xpanse.plugins.flexibleengine")
public class FlexibleEnginePluginProperties {

    private Boolean enableSdkHttpDebugLogs;
    private ServiceTemplate serviceTemplate;

    /** properties class. */
    @Data
    public static class ServiceTemplate {
        private Boolean autoApprove;
    }
}
