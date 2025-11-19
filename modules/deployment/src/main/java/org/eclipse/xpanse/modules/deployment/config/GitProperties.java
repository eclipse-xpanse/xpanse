/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** properties class. */
@Data
@ConfigurationProperties(prefix = "xpanse.git")
public class GitProperties {

    private Integer commandTimeoutSeconds;
}
