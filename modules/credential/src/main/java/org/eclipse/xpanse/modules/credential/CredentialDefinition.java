/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.enums.Csp;

/**
 * List credential definition that can be provided from end user.
 */
public class CredentialDefinition extends AbstractCredentialInfo {

    /**
     * The variables list of the credential.
     */
    @Getter
    @NotNull
    @Schema(description = "The variables list of the credential.")
    private final List<CredentialVariable> variables;


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
    public CredentialDefinition(Csp csp, String name, String description, CredentialType type,
                                List<CredentialVariable> variables) {
        super(csp, name, description, type);
        this.variables = variables;
    }
}
