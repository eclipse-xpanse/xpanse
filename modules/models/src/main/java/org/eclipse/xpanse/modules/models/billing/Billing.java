/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.billing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.hibernate.validator.constraints.UniqueElements;

/** Defines the billing model of the managed service. */
@Data
public class Billing {

    @Serial private static final long serialVersionUID = 240913796673011260L;

    @NotNull
    @NotEmpty
    @UniqueElements
    @Schema(
            description =
                    "Supported billing modes by the managed service. "
                            + "The list elements must be unique.")
    private List<BillingMode> billingModes;

    @Schema(
            description =
                    " This is used only for display purposes. When provided, this "
                            + "billingMode will be selected in the frontends by default.")
    private BillingMode defaultBillingMode;
}
