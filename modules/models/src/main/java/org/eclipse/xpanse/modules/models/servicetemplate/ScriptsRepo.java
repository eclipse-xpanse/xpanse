/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Data model to represent GIT repo details to fetch deployment scripts. */
@Data
public class ScriptsRepo {

    @NotNull
    @Schema(description = "URL of the repo")
    private String repoUrl;

    @NotNull
    @Schema(description = "Branch to be checked out. Can be branch or a Tag")
    private String branch;

    @Schema(
            description =
                    "Directory in the repo where scripts are present. "
                            + "If not provided, the root directory of the repo is considered")
    private String scriptsPath;
}
