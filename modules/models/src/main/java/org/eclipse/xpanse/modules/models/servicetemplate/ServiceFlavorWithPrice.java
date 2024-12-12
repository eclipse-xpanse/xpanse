/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eclipse.xpanse.modules.models.billing.RatingMode;

/** Defines for service flavor with price. */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ServiceFlavorWithPrice extends ServiceFlavor {

    @NotNull
    @Schema(description = "The pricing of the flavor")
    private RatingMode pricing;
}
