/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstacktestlab.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/** properties class. */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "xpanse.plugins.openstacktestlab")
public class OpenstackTestlabPluginProperties {

    private ServiceTemplate serviceTemplate;

    /** properties class. */
    @Data
    public static class ServiceTemplate {
        private Boolean autoApprove;
    }
}
