/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.billing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * Defines the price data model with specific region.
 */
@Data
public class PriceWithRegion implements Serializable {

    @Serial
    private static final long serialVersionUID = 4470758346696951771L;

    @NotNull
    @NotEmpty
    @NotBlank
    @Schema(description = "The defined region. If the special name 'any' provided, "
            + "this price for all unknown regions.")
    private String region;

    @NotNull
    @Schema(description = "The price for the defined region.")
    private Price price;


}
