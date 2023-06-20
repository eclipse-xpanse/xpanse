/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.register;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.register.enums.DeployerKind;

/**
 * Defines the Deployment.
 */
@Data
public class Deployment {

    @NotNull
    @Schema(description = "The type of the Deployer which will handle the service deployment")
    private DeployerKind kind;

    @Valid
    @NotNull
    @Schema(description = "The variables for the deployment, which will be passed to the deployer")
    private List<DeployVariable> variables;

    @Schema(description = "The credential type to do the deployment")
    private CredentialType credentialType = CredentialType.VARIABLES;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The real deployer, something like terraform scripts...")
    private String deployer;

}
