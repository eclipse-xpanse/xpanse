/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.billing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;

/** Defines the data model for the same resource. */
@Data
public class Resource {

    @NotNull
    @Schema(description = "The count of the same resource.")
    private int count;

    @NotNull
    @Schema(description = "The kind of the same resource.")
    private DeployResourceKind deployResourceKind;

    @Schema(description = "The properties of the same resource.")
    private Map<String, String> properties;
}
