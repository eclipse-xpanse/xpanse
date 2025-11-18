/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.regiocloud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/** properties class. */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "xpanse.plugins.regiocloud")
public class RegioCloudPluginProperties {

    private ServiceTemplate serviceTemplate;

    /** properties class. */
    @Data
    public static class ServiceTemplate {
        private Boolean autoApprove;
    }
}
