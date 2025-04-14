/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/** Defines Coordinates of Helm Chart. */
@Data
public class HelmCoordinates implements Serializable {

    @Serial private static final long serialVersionUID = -7340645290453759942L;

    @NotNull
    @NotBlank
    @Schema(description = "The repository of helm chart.")
    private String repository;

    @NotNull
    @NotBlank
    @Schema(description = "The version of the helm chart.")
    private String version;
}
