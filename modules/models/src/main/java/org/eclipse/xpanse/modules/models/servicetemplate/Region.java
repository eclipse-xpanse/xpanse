/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/** The regions of the Cloud Service Provider. */
@Data
public class Region implements Serializable {

    @Serial private static final long serialVersionUID = -908536837247974803L;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The name of the region")
    private String name;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(
            description =
                    "The site with the region belongs to, such as default, Chinese Mainland, "
                            + "International, ")
    private String site = "default";

    @NotEmpty
    @NotBlank
    @Schema(description = "The area which the region belongs to, such as Asia, Europe, Africa")
    private String area = "Others";
}
