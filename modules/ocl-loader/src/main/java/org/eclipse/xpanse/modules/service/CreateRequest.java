/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.service;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
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
    @Id
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
    String name;

    /**
     * The version of the Service.
     */
    @NotNull
    @NotBlank
    String version;

    /**
     * The csp of the Service.
     */
    @NotNull
    Csp csp;

    /**
     * The flavor of the Service.
     */
    @NotNull
    @NotBlank
    String flavor;

    /**
     * The property of the Service.
     */
    Map<String, String> property = new HashMap<>();
}
