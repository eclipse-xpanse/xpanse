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

/** Defines the price data model with specific region. */
@Data
public class PriceWithRegion implements Serializable {

    @Serial private static final long serialVersionUID = 4470758346696951771L;

    @NotNull
    @NotEmpty
    @NotBlank
    @Schema(
            description =
                    "The defined region name. If the special name 'any' provided, "
                            + "this price for all unknown regions.")
    private String regionName;

    @NotNull
    @NotEmpty
    @NotBlank
    @Schema(
            description =
                    "The defined site name. If the special name 'default' provided, "
                            + "this price for all sites.")
    private String siteName;

    @NotNull
    @Schema(description = "The price for the defined region and the defined site.")
    private Price price;
}
