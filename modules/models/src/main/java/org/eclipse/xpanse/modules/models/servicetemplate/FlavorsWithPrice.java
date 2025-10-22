/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.models.servicetemplate.controller.ApiMethodConfig;
import org.hibernate.validator.constraints.UniqueElements;

/** Defines for service flavors with price. */
@Data
public class FlavorsWithPrice {

    @Valid
    @NotNull
    @UniqueElements
    @Schema(description = "The flavors of the managed service. The list elements must be unique.")
    List<ServiceFlavorWithPrice> serviceFlavors;

    @NotNull
    @Schema(description = "Impact on service when flavor is changed.")
    private ModificationImpact modificationImpact;

    @NotNull
    @Schema(description = "Whether the downgrading is allowed, default value: true.")
    private Boolean isDowngradeAllowed = true;

    @Schema(description = "Flavor change method config in the service controller API layer.")
    private ApiMethodConfig apiMethodConfig;

    public Boolean isDowngradeAllowed() {
        return isDowngradeAllowed;
    }

    public void setDowngradeAllowed(Boolean downgradeAllowed) {
        isDowngradeAllowed = downgradeAllowed;
    }
}
