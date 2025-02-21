/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicechange;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** The result of ansible task. */
@Data
public class AnsibleTaskResult {

    @NotNull
    @Schema(description = "name of the Ansible task")
    private String name;

    @NotNull
    @Schema(description = "Depicts if the task is successful")
    private Boolean isSuccessful;

    @Schema(
            description =
                    "Data from the task. Will be returned both for successful and failure cases.")
    private String message;
}
