/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.credential;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;

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
     * The type of the credential.
     */
    @Getter
    @NotNull
    @Schema(description = "The type of the credential,"
            + "this field is provided by  he the plugin of cloud service provider.")
    CredentialType type;

    /**
     * The name of the credential, this field is provided by the plugins.
     */
    @Getter
    @NotNull
    @NotBlank
    @Schema(description = "The name of the credential, this field is provided by "
            + "the plugin of cloud service provider. The value of this field must be unique "
            + "between credentials with the same csp and type.")
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
     * The id of user who create the credential.
     */
    @Getter
    @Setter
    @Schema(description = "The id of user who created the credential.")
    String userId;

    /**
     * The expired unix long time of the credential.
     */
    @Getter
    @Setter
    @Hidden
    @Schema(description = "The time in seconds to live of the credential")
    Integer timeToLive;

    /**
     * The constructor.
     */
    AbstractCredentialInfo(Csp csp, CredentialType type, String name, String description,
                           String userId) {
        this.csp = csp;
        this.type = type;
        this.name = name;
        this.description = description;
        this.userId = userId;
    }

    /**
     * Get the unique key of AbstractCredentialInfo.
     *
     * @return the unique key joined by csp, type and name
     */
    @Hidden
    public String getUniqueKey() {
        return this.csp.name() + "-" + this.type.name() + "-" + this.name;
    }
}
