/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/** properties class. */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "xpanse.git")
public class GitProperties {

    private Integer commandTimeoutSeconds;
}
