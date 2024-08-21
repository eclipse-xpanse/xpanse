/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Defines the config manage scripts for agent.
 */

@Valid
@Data
@Slf4j
public class ConfigManageScript implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "should be the name of the resource available in the deployer script.")
    private String configManager;

    @NotNull
    @Schema(description = "Means should the configuration update run on each node "
            + "of the specific component or just one.")
    private Boolean runOnlyOnce;

    @NotNull
    @Schema(description = "Ansible script configuration details.")
    private AnsibleScriptConfig ansibleScriptConfig;
}
