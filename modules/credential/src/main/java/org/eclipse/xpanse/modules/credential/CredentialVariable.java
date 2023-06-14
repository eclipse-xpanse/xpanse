/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eclipse.xpanse.modules.credential.config.CredentialVariableDeserializer;

/**
 * The class object for the CredentialVariable.
 */
@Data
@SuppressWarnings("UnnecessarilyFullyQualified")
@JsonDeserialize(using = CredentialVariableDeserializer.class)
public class CredentialVariable {

    @NotNull
    @Schema(description = "The name of the CredentialVariable,"
            + "this field is provided by the the plugin of cloud service provider.")
    private final String name;

    @NotNull
    @Schema(description = "The description of the CredentialVariable,"
            + "this field is provided by the plugin of cloud service provider.")
    private final String description;

    @Schema(description = "If the variable is mandatory. If is optional then the credential "
            + "completeness check will ignore this variable. It is upto the plugin to decide what "
            + "needs to be done if this optional credential variable is present.",
            defaultValue = "true")
    private final boolean isMandatory;

    @NotNull
    @Schema(description = "Defines if the particular variable contains sensitive data. For example "
            + "the value is false for username and true for password variables respectively.")
    private final boolean isSensitive;

    @NotNull
    @NotBlank
    @Schema(description = "The value of the CredentialVariable, this field is filled by the user.")
    private String value;

    /**
     * Constructor for default mandatory variables.
     *
     * @param name        name of the variable.
     * @param description description of the variable.
     */
    public CredentialVariable(String name, String description, boolean isSensitive) {
        this.name = name;
        this.description = description;
        this.isMandatory = true;
        this.isSensitive = isSensitive;
    }

    /**
     * Constructor to set mandatory flag explicitly for the variables.
     *
     * @param name        name of the variable.
     * @param description description of the variable.
     * @param isMandatory if the credential variable is mandatory.
     */
    public CredentialVariable(String name, String description, boolean isMandatory,
                              boolean isSensitive) {
        this.name = name;
        this.description = description;
        this.isMandatory = isMandatory;
        this.isSensitive = isSensitive;
    }

    /**
     * Constructor to initialize all properties of the CredentialVariable.
     *
     * @param name        name of the variable.
     * @param description description of the variable.
     * @param isMandatory if the credential variable is mandatory.
     * @param isSensitive defines if the particular variable contains sensitive data.
     * @param value       the value of the CredentialVariable filled by the user.
     */
    public CredentialVariable(String name, String description, boolean isMandatory,
                              boolean isSensitive, String value) {
        this.name = name;
        this.description = description;
        this.isMandatory = isMandatory;
        this.isSensitive = isSensitive;
        this.value = value;
    }
}
