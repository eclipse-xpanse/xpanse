/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.billing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;

/**
 * Defines the billing model of the managed service.
 */
@Data
public class Billing implements Serializable {

    @Serial
    private static final long serialVersionUID = 240913796673011260L;

    @NotNull
    @Schema(description = "Supported billing modes by the managed service")
    private List<BillingMode> billingModes;
}
