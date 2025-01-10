/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deployment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;

/** DeployResource model. */
@Data
public class DeployResource {

    @NotNull
    @NotBlank
    @Schema(description = "The type of the group which configuration the deployed resource.")
    private String groupType;

    @NotNull
    @NotBlank
    @Schema(description = "The name of the group which configuration the deployed resource.")
    private String groupName;

    @NotNull
    @NotBlank
    @Schema(description = "The id of the deployed resource.")
    private String resourceId;

    @NotNull
    @NotBlank
    @Schema(description = "The name of the deployed resource.")
    private String resourceName;

    @NotNull
    @Schema(description = "The kind of the deployed resource.")
    private DeployResourceKind resourceKind;

    @NotNull
    @Schema(description = "The properties of the deployed resource.")
    private Map<String, String> properties;
}
