/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class of local terraform env.
 */
@Configuration
public class TerraformLocalConfig {

    @Getter
    @Value("${terraform.workspace.directory}")
    private String workspaceDirectory;

    @Getter
    @Value("${terraform.debug.enabled:false}")
    private boolean isDebugEnabled;

    @Getter
    @Value("${terraform.debug.level:DEBUG}")
    private String debugLogLevel;
}
