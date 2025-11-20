/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.plusserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** properties class. */
@Data
@ConfigurationProperties(prefix = "xpanse.plugins.plusserver")
public class PlusServerPluginProperties {

    private ServiceTemplate serviceTemplate;

    /** properties class. */
    @Data
    public static class ServiceTemplate {
        private Boolean autoApprove;
    }
}
