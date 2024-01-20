/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */


package org.eclipse.xpanse.modules.models.policy.userpolicy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eclipse.xpanse.modules.models.common.enums.Csp;

/**
 * The model for creating the policy.
 */
@Data
public class UserPolicyCreateRequest {

    /**
     * The csp which the policy belongs to.
     */
    @NotNull
    @Schema(description = "The csp which the policy belongs to.")
    private Csp csp;

    /**
     * The policy.
     */
    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The policy.")
    private String policy;


    /**
     * Is the policy enabled.
     */
    @NotNull
    @Schema(description = "Is the policy enabled. true:enabled;false:disabled.")
    private Boolean enabled = true;

}
