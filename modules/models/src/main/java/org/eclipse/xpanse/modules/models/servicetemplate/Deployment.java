/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.validation.annotation.Validated;

/**
 * Defines the Deployment.
 */
@Data
@Validated
public class Deployment implements Serializable {

    @Serial
    private static final long serialVersionUID = 2566478948717883360L;

    @Valid
    @NotNull
    @Schema(description = "The deployer tool which will handle the service deployment.")
    private DeployerTool deployerTool;

    @Valid
    @NotNull
    @NotEmpty
    @UniqueElements
    @Schema(description = "The variables for the deployment, which will be passed to the deployer."
            + "The list elements must be unique.")
    private List<DeployVariable> variables;

    @Schema(description = "The credential type to do the deployment")
    private CredentialType credentialType = CredentialType.VARIABLES;

    @Valid
    @Size(min = 1)
    @UniqueElements
    @Schema(description = "The list of availability zone configuration of the service."
            + "The list elements must be unique.")
    private List<AvailabilityZoneConfig> serviceAvailabilityConfig;

    @Schema(description = "The real deployer, something like terraform scripts. "
            + "Either deployer or deployFromGitRepo must be provided.")
    private String deployer;

    @Schema(description = "Deployment scripts hosted on a GIT repo. "
            + "Either deployer or deployFromGitRepo must be provided.")
    private ScriptsRepo scriptsRepo;

}
