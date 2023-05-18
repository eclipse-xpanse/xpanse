/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * The class object for the CredentialVariable.
 */
@Data
public class CredentialVariable {

    /**
     * The name of the CredentialVariable.
     */
    @NotNull
    @Schema(description = "The name of the CredentialVariable,"
            + "this field is provided by the the plugin of cloud service provider.")
    private final String name;

    /**
     * The description of the CredentialVariable.
     */
    @NotNull
    @Schema(description = "The description of the CredentialVariable,"
            + "this field is provided by the plugin of cloud service provider.")
    private final String description;

    /**
     * The value of the CredentialVariable.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The value of the CredentialVariable, this field is filled by the user.")
    private String value;

    /**
     * The constructor.
     */
    public CredentialVariable(String name, String description) {
        this.name = name;
        this.description = description;
    }

}
