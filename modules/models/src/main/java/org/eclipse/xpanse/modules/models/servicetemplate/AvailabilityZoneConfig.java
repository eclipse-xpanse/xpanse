/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Defines the availability zone model of the managed service. */
@Data
public class AvailabilityZoneConfig {

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The display name of availability zone.")
    private String displayName;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The variable name of availability zone.")
    private String varName;

    @NotNull
    @Schema(description = "Indicates if the variable is mandatory.")
    private Boolean mandatory;

    @Schema(description = "The description of availability zone.")
    private String description;
}
