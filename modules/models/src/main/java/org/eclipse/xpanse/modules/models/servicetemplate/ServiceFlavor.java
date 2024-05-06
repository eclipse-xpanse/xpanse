/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
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
    @NotEmpty
    @Schema(description = "The properties of the flavor")
    private Map<String, String> properties;

    @NotNull
    @Min(value = 1, message = "The minimum value of priority cannot be less than 1.")
    @Schema(description = "The priority of the flavor. The larger value means lower priority.")
    private Integer priority;
}
