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

/**
 * Defines for service flavor.
 */
@Data
public class FlavorBasic {

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The flavor name")
    private String name;

    @NotNull
    @Schema(description = "The price of the flavor")
    private Integer fixedPrice;

}
