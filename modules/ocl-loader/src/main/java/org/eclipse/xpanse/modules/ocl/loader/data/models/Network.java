/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * Defines the network details on which the managed service is deployed.
 */
@Data
public class Network {

    @Valid
    @NotNull
    @NotEmpty
    @Schema(description = "The list of vpc in the network")
    private List<Vpc> vpc;

    @Valid
    @NotNull
    @NotEmpty
    @Schema(description = "The list of subnets in the network for the @vpc")
    private List<Subnet> subnets;

    @Valid
    @NotEmpty
    @Schema(description = "The list of security groups for the VMs")
    private List<SecurityGroup> securityGroups;

}
