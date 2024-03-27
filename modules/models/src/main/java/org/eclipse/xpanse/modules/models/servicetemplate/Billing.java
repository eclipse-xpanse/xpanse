/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Data;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.BillingModel;

/**
 * Defines the billing model of the managed service.
 */
@Data
public class Billing implements Serializable {

    @NotNull
    @Schema(description = "The billing model of the managed service")
    private BillingModel billingModel;

}
