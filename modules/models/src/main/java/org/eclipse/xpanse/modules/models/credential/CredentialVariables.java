/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.credential;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;

/**
 * List credential definition that can be provided from end user.
 */
@Getter
public class CredentialVariables extends AbstractCredentialInfo {

    /**
     * The variables list of the credential.
     */
    @NotNull
    @Size(min = 1)
    @Schema(description = "The variables list of the credential.")
    private final List<CredentialVariable> variables;

    /**
     * The constructor without filed timeToLive.
     */
    public CredentialVariables(Csp csp, CredentialType type, String name, String description,
                               String userId, List<CredentialVariable> variables) {
        super(csp, type, name, description, userId);
        this.variables = variables;
    }

    /**
     * The constructor with all fields.
     */
    @JsonCreator
    public CredentialVariables(@JsonProperty("csp") Csp csp,
                               @JsonProperty("type") CredentialType type,
                               @JsonProperty("name") String name,
                               @JsonProperty("description") String description,
                               @JsonProperty("userId") String userId,
                               @JsonProperty("timeToLive") Integer timeToLive,
                               @JsonProperty("variables") List<CredentialVariable> variables) {
        super(csp, type, name, description, userId);
        super.timeToLive = timeToLive;
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
