/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.enums.Category;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.resource.Ocl;

/**
 * Request body for service creation.
 */
@Valid
@Data
public class CreateRequest {

    @Hidden
    private UUID id;

    /**
     * The category of the Service.
     */
    @NotNull
    @Schema(description = "The category of the service")
    private Category category;

    /**
     * The name of the Service.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The name of the service")
    private String name;

    /**
     * The version of the Service.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The version of service")
    private String version;


    @NotNull
    @NotBlank
    @Schema(description = "The region of the provider.")
    private String region;

    /**
     * The csp of the Service.
     */
    @NotNull
    @Schema(description = "The csp of the Service.")
    private Csp csp;

    /**
     * The flavor of the Service.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The flavor of the Service.")
    private String flavor;

    @Hidden
    private Ocl ocl;

    /**
     * The property of the Service.
     */
    @Schema(description = "The property of the Service")
    private Map<String, String> property;
}
