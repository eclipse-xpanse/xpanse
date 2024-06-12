/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.billing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * Defines the data model for the resource usage.
 */
@Data
public class ResourceUsage implements Serializable {

    @Serial
    private static final long serialVersionUID = 240913796673011260L;

    @NotNull
    @NotEmpty
    @Schema(description = "The resources of the flavor of the manged service.")
    private List<Resource> resources;

    @Schema(description = "The license price of the flavor of the manged service.")
    private Price licensePrice;

    @Schema(description = "The listed price of the flavor of the manged service.")
    private Price markUpPrice;
}
