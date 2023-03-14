/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.view;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.models.enums.Csp;


/**
 * Define view object for UI Client query registered service by csp.
 */
@Data
public class ProviderOclVo {

    @NotNull
    @Schema(description = "The Cloud Service Provider.")
    private Csp name;

    @NotNull
    @Schema(description = "The regions of the Cloud Service Provider.")
    private List<String> regions;

    @NotNull
    @Schema(description = "The list of the registered services.")
    private List<OclDetailVo> details;

}
