/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.register;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * The regions of the Cloud Service Provider.
 */
@Data
public class Region {

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The name of the Region")
    private String name;

    @NotEmpty
    @NotBlank
    @Schema(description = "The area which the region belongs to, such as Asia, Europe, Africa")
    private String area = "Others";
}
