/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.view;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.register.Region;


/**
 * View object for list available services group by csp.
 */
@Data
public class ProviderOclVo {

    @NotNull
    @Schema(description = "The Cloud Service Provider.")
    private Csp name;

    @NotNull
    @Schema(description = "The regions of the Cloud Service Provider.")
    private List<@Valid Region> regions;

    @NotNull
    @Schema(description = "The list of the available services.")
    private List<@Valid UserAvailableServiceVo> details;

}
