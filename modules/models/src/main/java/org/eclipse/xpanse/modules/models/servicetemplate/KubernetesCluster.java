/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.KubernetesClusterType;

/** Defines KubernetesCluster. */
@Data
public class KubernetesCluster {

    @NotNull
    @NotBlank
    @Schema(description = "The version of the kubernetes.")
    private String version;

    @NotNull
    @Schema(description = "The type of the kubernetes cluster.")
    private KubernetesClusterType type;
}
