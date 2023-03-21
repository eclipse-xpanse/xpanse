/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * Define the area of the region.
 */
@Data
public class Area {

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The name of the area")
    private String name;

    @NotNull
    @NotEmpty
    @Schema(description = "The regions of the area")
    private List<String> regions;

}
