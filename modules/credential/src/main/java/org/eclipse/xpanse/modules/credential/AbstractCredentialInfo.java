/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.enums.Csp;

/**
 * The Abstract class which defines the credential basic information required by a cloud provider.
 */
@Schema(allOf = {CredentialVariables.class})
public abstract class AbstractCredentialInfo {

    /**
     * The cloud service provider of the credential, this field is provided by the plugins.
     */
    @Getter
    @Setter
    @NotNull
    @Schema(description = "The cloud service provider of the credential.")
    Csp csp;

    /**
     * The user who create the credential.
     */
    @Getter
    @Setter
    @NotNull
    @Schema(description = "The user who create the credential.")
    String xpanseUser;

    /**
     * The name of the credential, this field is provided by the plugins.
     */
    @Getter
    @NotNull
    @NotBlank
    @Schema(description = "The name of the credential,"
            + "this field is provided by  he the plugin of cloud service provider.")
    String name;

    /**
     * The description of the credential.
     */
    @Getter
    @NotNull
    @NotBlank
    @Schema(description = "The description of the credential,"
            + "this field is provided by  he the plugin of cloud service provider.")
    String description;

    /**
     * The type of the credential.
     */
    @Getter
    @NotNull
    @Schema(description = "The type of the credential,"
            + "this field is provided by  he the plugin of cloud service provider.")
    CredentialType type;

    /**
     * The expired unix long time of the credential.
     */
    @Getter
    @Setter
    @Hidden
    private Long expiredTime;

    /**
     * The constructor.
     */
    AbstractCredentialInfo(Csp csp, String xpanseUser, String name, String description,
                           CredentialType type) {
        this.csp = csp;
        this.xpanseUser = xpanseUser;
        this.name = name;
        this.description = description;
        this.type = type;
    }
}
