/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class of local open tofu env.
 */
@Getter
@Configuration
public class OpenTofuLocalConfig {

    @Value("${opentofu.workspace.directory}")
    private String workspaceDirectory;

    @Value("${opentofu.debug.enabled:false}")
    private boolean isDebugEnabled;

    @Value("${opentofu.debug.level:DEBUG}")
    private String debugLogLevel;
}
