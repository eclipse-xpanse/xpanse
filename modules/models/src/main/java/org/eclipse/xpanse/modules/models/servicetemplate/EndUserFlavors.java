/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Defines service flavors for end user. we need a separate data model to end user since the
 * original flavor data model is used by ISV and CSP also have the pricing configuration info which
 * should not be returned to the end user.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EndUserFlavors extends ServiceFlavor {

    @NotNull
    @Schema(description = "The flavors of the orderable service.")
    private List<ServiceFlavor> serviceFlavors;

    @NotNull
    @Schema(description = "Impact on service when flavor is changed.")
    private ModificationImpact modificationImpact;

    @NotNull
    @Schema(description = "Whether the downgrading is allowed, default value: true.")
    private Boolean isDowngradeAllowed = true;

    public Boolean isDowngradeAllowed() {
        return isDowngradeAllowed;
    }

    public void setDowngradeAllowed(Boolean downgradeAllowed) {
        isDowngradeAllowed = downgradeAllowed;
    }
}
