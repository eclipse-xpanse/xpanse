/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.policy.servicepolicy;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/** The model for updating the policy. */
@Data
public class ServicePolicyUpdateRequest {
    /** The flavor name list which the policy belongs to. */
    @Schema(
            description =
                    "The flavor names to which the policy belongs. If the list is empty, then"
                            + " the policy will be executed for during service deployment of all"
                            + " flavors.")
    private List<String> flavorNames;

    /** The policy. */
    @Schema(description = "The policy.")
    private String policy;

    /** Is the policy enabled. */
    @Schema(description = "Is the policy enabled. true:enabled;false:disabled.")
    private Boolean enabled;
}
