/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.hibernate.validator.constraints.UniqueElements;

/** Defines the Deployment. */
@Data
public class Deployment {

    @Valid
    @NotNull
    @Schema(description = "The deployer tool which will handle the service deployment.")
    private DeployerTool deployerTool;

    @Schema(description = "The credential type to do the deployment")
    private CredentialType credentialType = CredentialType.VARIABLES;

    @Valid
    @Size(min = 1)
    @UniqueElements
    @Schema(
            description =
                    "The list of availability zone configuration of the service."
                            + "The list elements must be unique.")
    private List<AvailabilityZoneConfig> serviceAvailabilityConfig;

    @Schema(
            description =
                    "Deployment information for terraform based deployments. "
                            + "This is mandatory if the deployer kind is Terraform or OpenTofu")
    private TerraformDeployment terraformDeployment;

    @Schema(
            description =
                    "Deployment information for Helm based deployments. "
                            + "This is mandatory if the deployer kind is Helm")
    private HelmDeployment helmDeployment;
}
