/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */


package org.eclipse.xpanse.modules.models.policy.userpolicy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;

/**
 * The model for updating the policy.
 */
@Data
public class UserPolicyUpdateRequest {

    /**
     * The id of the policy.
     */
    @NotNull
    @Schema(description = "The id of the policy.")
    private UUID id;

    /**
     * The csp which the policy belongs to.
     */
    @Schema(description = "The csp which the policy belongs to.")
    private Csp csp;

    /**
     * The policy.
     */
    @Schema(description = "The policy.")
    private String policy;

    /**
     * Is the policy enabled.
     */
    @Schema(description = "Is the policy enabled. true:enabled;false:disabled.")
    private Boolean enabled;

}
