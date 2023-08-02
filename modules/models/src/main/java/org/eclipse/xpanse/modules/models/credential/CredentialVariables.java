/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.credential;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;

/**
 * List credential definition that can be provided from end user.
 */
public class CredentialVariables extends AbstractCredentialInfo {

    /**
     * The variables list of the credential.
     */
    @Getter
    @NotNull
    @Schema(description = "The variables list of the credential.")
    private final List<CredentialVariable> variables;

    /**
     * The constructor.
     */
    public CredentialVariables(Csp csp, CredentialType type, String name, String description,
                               String userId, List<CredentialVariable> variables) {
        super(csp, type, name, description, userId);
        this.variables = variables;
    }

    /**
     * The constructor.
     */
    public CredentialVariables(CreateCredential createCredential) {
        super(createCredential.getCsp(), createCredential.getType(),
                createCredential.getName(), createCredential.getDescription(),
                createCredential.getUserId());
        super.timeToLive = createCredential.getTimeToLive();
        this.variables = createCredential.getVariables();
    }
}
