/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */


package org.eclipse.xpanse.modules.models.policy.servicepolicy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Data;

/**
 * The model for creating the policy.
 */
@Data
public class ServicePolicyCreateRequest {

    /**
     * The id of registered service template which the policy belongs to.
     */
    @NotNull
    @Schema(description = "The id of registered service template which the policy belongs to.")
    private UUID serviceTemplateId;

    /**
     * The flavor name list which the policy belongs to.
     */
    @Schema(description =
            "The flavor name list which the policy belongs to. If the list is empty, then the "
                    + "policy will be executed for during service deployment of all flavors.")
    private List<String> flavorNameList;

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
