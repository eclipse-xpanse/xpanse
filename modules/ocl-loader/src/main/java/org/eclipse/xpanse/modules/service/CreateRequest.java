/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.service;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.Category;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.Csp;

/**
 * Request body for service creation.
 */
@Data
public class CreateRequest {

    @Hidden
    private UUID id;

    /**
     * The category of the Service.
     */
    @NotNull
    Category category;

    /**
     * The name of the Service.
     */
    @NotNull
    @NotBlank
    private String name;

    /**
     * The version of the Service.
     */
    @NotNull
    @NotBlank
    private String version;

    /**
     * The csp of the Service.
     */
    @NotNull
    private Csp csp;

    /**
     * The flavor of the Service.
     */
    @NotNull
    @NotBlank
    private String flavor;

    /**
     * The property of the Service.
     */
    private Map<String, String> property;
}
