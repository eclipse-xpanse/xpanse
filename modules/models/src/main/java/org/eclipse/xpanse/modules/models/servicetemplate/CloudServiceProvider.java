/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.hibernate.validator.constraints.UniqueElements;

/** Defines for the Cloud Service Provider. */
@Data
public class CloudServiceProvider {

    @NotNull
    @Schema(description = "The Cloud Service Provider.")
    private Csp name;

    @Valid
    @NotNull
    @NotEmpty
    @UniqueElements
    @Schema(
            description =
                    "The regions of the Cloud Service Provider. "
                            + "The list elements must be unique.")
    private List<Region> regions;
}
