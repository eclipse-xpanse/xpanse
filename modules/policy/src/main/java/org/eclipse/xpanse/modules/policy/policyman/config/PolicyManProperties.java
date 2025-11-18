/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.policy.policyman.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Properties class. */
@Data
@ConfigurationProperties(prefix = "xpanse.policy-man")
public class PolicyManProperties {

    private String endpoint;
}
