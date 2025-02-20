/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicechange;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/** The result of Service change. */
@Data
public class ServiceChangeResult {

    @NotNull
    @Schema(description = "describes if the change is successfully executed.")
    private Boolean isSuccessful;

    @Schema(description = "error description if the task failed.")
    private String error;

    @Schema(description = "describes result of each Ansible task executed by the agent.")
    private List<AnsibleTaskResult> tasks;
}
