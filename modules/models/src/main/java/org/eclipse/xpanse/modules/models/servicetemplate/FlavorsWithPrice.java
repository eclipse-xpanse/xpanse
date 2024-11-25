/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import org.hibernate.validator.constraints.UniqueElements;

/**
 * Defines for service flavors with price.
 */
@Data
public class FlavorsWithPrice implements Serializable {

    @Serial
    private static final long serialVersionUID = 6980257522875080048L;

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

    public Boolean isDowngradeAllowed() {
        return isDowngradeAllowed;
    }

    public void setDowngradeAllowed(Boolean downgradeAllowed) {
        isDowngradeAllowed = downgradeAllowed;
    }
}
