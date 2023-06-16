/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.plugin.credential;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.plugin.credential.enums.CredentialType;

/**
 * Create credential model.
 */
@Data
public class CreateCredential {

    @NotNull
    @NotBlank
    @Schema(description = "The name of the credential")
    private String name;

    @NotNull
    @NotBlank
    @Schema(description = "The user who create the credential.")
    private String xpanseUser;

    @Schema(description = "The cloud service provider of the credential.")
    @NotNull
    private Csp csp;

    @Schema(description = "The description of the credential")
    private String description;

    @NotNull
    @Schema(description = "The type of the credential")
    private CredentialType type;

    @NotNull
    @Schema(description = "The variables list of the credential")
    private List<CredentialVariable> variables;

    @NotNull
    @Schema(description = "The time in seconds to live of the credential", defaultValue = "3600")
    private Integer timeToLive = 3600;

}
