/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.springframework.validation.annotation.Validated;

/**
 * Defines the Deployment.
 */
@Data
@Validated
public class Deployment implements Serializable {

    @Serial
    private static final long serialVersionUID = 2566478948717883360L;

    @NotNull
    @Schema(description = "The type of the Deployer which will handle the service deployment")
    private DeployerKind kind;

    @Valid
    @NotNull
    @Schema(description = "The variables for the deployment, which will be passed to the deployer")
    private List<DeployVariable> variables;

    @Schema(description = "The credential type to do the deployment")
    private CredentialType credentialType = CredentialType.VARIABLES;

    @Schema(description = "The real deployer, something like terraform scripts. "
            + "Either deployer or deployFromGitRepo must be provided.")
    private String deployer;

    @Schema(description = "Deployment scripts hosted on a GIT repo. "
            + "Either deployer or deployFromGitRepo must be provided.")
    private ScriptsRepo scriptsRepo;

}
