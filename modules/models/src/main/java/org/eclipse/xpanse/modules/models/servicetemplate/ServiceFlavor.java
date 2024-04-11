/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import lombok.Data;

/**
 * Defines for service flavor.
 */
@Data
public class ServiceFlavor implements Serializable {

    @Serial
    private static final long serialVersionUID = 7178375302626204744L;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The flavor name")
    private String name;

    @NotNull
    @Schema(description = "The price of the flavor")
    private Integer fixedPrice;

    @NotNull
    @NotEmpty
    @Schema(description = "The properties of the flavor")
    private Map<String, String> properties;

}
