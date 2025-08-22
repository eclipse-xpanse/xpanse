/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.credential;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;

/** List credential definition that can be provided from end user. */
@Getter
@SuppressFBWarnings(value = "RI_REDUNDANT_INTERFACES")
public class CredentialVariables extends AbstractCredentialInfo implements Cloneable {

    /** The variables list of the credential. */
    @NotNull
    @Size(min = 1)
    @Schema(description = "The variables list of the credential.")
    private List<CredentialVariable> variables;

    /** The constructor without filed timeToLive. */
    public CredentialVariables(
            Csp csp,
            String site,
            CredentialType type,
            String name,
            String description,
            String userId,
            List<CredentialVariable> variables) {
        super(csp, site, type, name, description, userId);
        this.variables = variables;
    }

    /** The constructor with all fields. */
    @JsonCreator
    public CredentialVariables(
            @JsonProperty("csp") Csp csp,
            @JsonProperty("site") String site,
            @JsonProperty("type") CredentialType type,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("userId") String userId,
            @JsonProperty("timeToLive") Integer timeToLive,
            @JsonProperty("variables") List<CredentialVariable> variables) {
        super(csp, site, type, name, description, userId);
        super.timeToLive = timeToLive;
        this.variables = variables;
    }

    /** The constructor. */
    public CredentialVariables(CreateCredential createCredential) {
        super(
                createCredential.getCsp(),
                createCredential.getSite(),
                createCredential.getType(),
                createCredential.getName(),
                createCredential.getDescription(),
                createCredential.getUserId());
        super.timeToLive = createCredential.getTimeToLive();
        this.variables = createCredential.getVariables();
    }

    @Override
    @SuppressFBWarnings(value = "MC_OVERRIDABLE_METHOD_CALL_IN_CLONE")
    public CredentialVariables clone() {
        CredentialVariables clone = (CredentialVariables) super.clone();
        List<CredentialVariable> newVariables = new ArrayList<>();
        clone.getVariables()
                .forEach(
                        variable -> {
                            newVariables.add(variable.clone());
                        });
        clone.variables = newVariables;
        return clone;
    }
}
