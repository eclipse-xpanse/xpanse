/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;
import org.eclipse.xpanse.modules.models.enums.DeployResourceKind;

/**
 * DeployResource model.
 */
@Data
public class DeployResource {

    /**
     * The ID of the deployed resource.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The id of the deployed resource.")
    private String resourceId;
    /**
     * The name of the deployed resource.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The name of the deployed resource.")
    private String name;
    /**
     * The kind of the deployed resource.
     */
    @NotNull
    @Schema(description = "The kind of the deployed resource.")
    private DeployResourceKind kind;

    /**
     * The property of the deployed resource.
     */
    @NotNull
    @Schema(description = "The property of the deployed resource.")
    private Map<String, String> property;

}
