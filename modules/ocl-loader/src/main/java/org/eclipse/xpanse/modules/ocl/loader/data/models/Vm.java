/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
 * Defines the VM configuration required for the managed service to be deployed.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Vm extends RuntimeBase {

    @Length(min = 3, max = 64)
    @Schema(description = "The name of the VM")
    private String name;

    @NotBlank
    @Schema(description = "The type of the VM, It should not be same for different Cloud provider.")
    private String type;

    @NotBlank
    @Schema(description =
            "The image id for the VM, for example: 3f4b7f78-a8f4-11ed-9e3c-d3d3ca352d5e")
    private String image;

    @NotBlank
    @Schema(description = "The subnets for the VM")
    private List<String> subnets;

    @Schema(description = "The security groups for the VM")
    private List<String> securityGroups;

    @Schema(description = "The storages for the VM")
    private List<String> storages;

    @Schema(description = "The publicly means either the VM needs a public ip")
    private boolean publicly;

    @Valid
    @Schema(description = "The userData for the VM")
    private UserData userData;

}
