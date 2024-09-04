/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.credential;


import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.hibernate.validator.constraints.UniqueElements;

/**
 * Create credential model.
 */
@Data
public class CreateCredential {

    @NotNull
    @NotBlank
    @Schema(description = "The name of the credential")
    private String name;

    @Hidden
    private String userId;

    @NotNull
    @Schema(description = "The cloud service provider of the credential.")
    private Csp csp;

    @NotNull
    @NotEmpty
    @Schema(description = "The site to which the credentials belong to.")
    private String site;

    @Schema(description = "The description of the credential")
    private String description;

    @NotNull
    @Schema(description = "The type of the credential")
    private CredentialType type;

    @NotNull
    @NotEmpty
    @UniqueElements
    @Schema(description = "The variables list of the credential. The list elements must be unique.")
    private List<CredentialVariable> variables;

    @NotNull
    @Schema(description = "The time in seconds to live of the credential", defaultValue = "3600")
    private Integer timeToLive = 3600;

}
