/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.credential;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.config.AbstractCredentialInfoDeserializer;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;

/**
 * The Abstract class which defines the credential basic information required by a cloud provider.
 */
@Getter
@Schema(allOf = {CredentialVariables.class})
@JsonDeserialize(using = AbstractCredentialInfoDeserializer.class)
public abstract class AbstractCredentialInfo {

    /**
     * The cloud service provider of the credential, this field is provided by the plugins.
     */
    @Setter
    @NotNull
    @Schema(description = "The cloud service provider of the credential.")
    Csp csp;

    @Setter
    @NotNull
    @NotBlank
    @Schema(description = "The site which the credentials belong to.")
    String site;

    /**
     * The type of the credential.
     */
    @NotNull
    @Schema(description = "The type of the credential, "
            + "this field is provided by the plugin of cloud service provider.")
    CredentialType type;

    /**
     * The name of the credential, this field is provided by the plugins.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The name of the credential, this field is provided by "
            + "the plugin of cloud service provider. The value of this field must be unique "
            + "between credentials with the same csp and type.")
    String name;

    /**
     * The description of the credential.
     */
    @NotNull
    @NotBlank
    @Schema(description = "The description of the credential,"
            + "this field is provided by the plugin of cloud service provider.")
    String description;

    /**
     * The id of user who create the credential.
     */
    @Setter
    @Schema(description = "The id of user who created the credential.")
    String userId;

    /**
     * The expired unix long time of the credential.
     */
    @Setter
    @Hidden
    @Schema(description = "The time in seconds to live of the credential")
    Integer timeToLive;

    /**
     * The constructor.
     */
    AbstractCredentialInfo(Csp csp, String site, CredentialType type, String name,
                           String description, String userId) {
        this.csp = csp;
        this.site = site;
        this.type = type;
        this.name = name;
        this.description = description;
        this.userId = userId;
    }

    /**
     * Get the unique key of AbstractCredentialInfo.
     *
     * @return the unique key joined by csp, site, type and name
     */
    @Hidden
    public String getUniqueKey() {
        return this.csp.name() + "-" + this.site + "-" + this.type.name() + "-" + this.name;
    }
}
