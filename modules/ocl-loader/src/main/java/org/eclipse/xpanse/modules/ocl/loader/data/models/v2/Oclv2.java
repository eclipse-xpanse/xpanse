/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * Defines for OCLv2.
 */
@Valid
@Data
public class Oclv2 {

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The version of the Ocl")
    private String version;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The name of the managed service")
    private String name;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The version of the managed service")
    private String serviceVersion;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The description of the managed service")
    private String description;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The namespace of the managed service")
    private String namespace;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The icon of the managed service")
    private String icon;

    @Valid
    @NotNull
    @Schema(description = "The cloud service provider of the managed service")
    private CloudServiceProvider cloudServiceProvider;

    @Valid
    @NotNull
    @Schema(description = "The deployment of the managed service")
    private Deployment deployment;

    @Valid
    @NotNull
    @Schema(description = "The flavors of the managed service")
    private List<Flavor> flavors;

    @Valid
    @NotNull
    @Schema(description = "The billing policy of the managed service")
    private Billing billing;
}
