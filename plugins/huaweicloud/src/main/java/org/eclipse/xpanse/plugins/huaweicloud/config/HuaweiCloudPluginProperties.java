/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.huaweicloud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** properties class. */
@Data
@ConfigurationProperties(prefix = "xpanse.plugins.huaweicloud")
public class HuaweiCloudPluginProperties {

    private Boolean enableSdkHttpDebugLogs;
    private ServiceTemplate serviceTemplate;
    private String internationalPriceCalculatorUrl;
    private String europeanPriceCalculatorUrl;
    private String chinesePriceCalculatorUrl;

    /** Properties class. */
    @Data
    public static class ServiceTemplate {
        private Boolean autoApprove;
    }
}
