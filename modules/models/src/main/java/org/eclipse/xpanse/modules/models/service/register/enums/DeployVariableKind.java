/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.register.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/**
 * Deploy variable kinds.
 */
@Schema(enumAsRef = true, description = """
        - `fix_env`: Values for variable of this type are defined by the managed service provider\s
        in the OCL template. Runtime will inject it to deployer as environment variables.\s
        This variable is not visible to the end user.
        - `fix_variable`: Values for variable of this type are defined by the managed service\s
        provider in the OCL template. Runtime will inject it to deployer as usual variables.\s
        This variable is not visible to the end user.
        - `env`: Value for a variable of this type can be provided by end user.\s
        If marked as mandatory then end user must provide value to this variable.\s
        If marked as optional and if end user does not provide it,\s
        then the fallback value to this variable is read by runtime (it can read from other sources,
         e.g., OS env variables). This variable is injected as an environment\s
         variable to the deployer.
        - `variable`: Value for a variable of this type can be provided by end user.\s
        If marked as mandatory then end user must provide value to this variable.\s
        If marked as optional and if end user does not provide it,\s
        then the fallback value to this variable is read by runtime (it can read from other sources,
         e.g., OS env variables). This variable is injected as a regular variable to the deployer.
        - `env_env`: Value to this variable is read by runtime\s
        (it can read from other sources, e.g., OS env variables)\s
        and injected as an environment variable to the deployer.\s
        End user cannot see or change this variable.
        - `env_variable`: Value to this variable is read by runtime\s
        (it can read from other sources, e.g., OS env variables)\s
        and injected as a regular variable to the deployer.\s
        End user cannot see or change this variable.""")
public enum DeployVariableKind {

    FIX_ENV("fix_env"),
    FIX_VARIABLE("fix_variable"),
    ENV("env"),
    VARIABLE("variable"),
    ENV_ENV("env_env"),
    ENV_VARIABLE("env_variable");

    private final String type;

    DeployVariableKind(String type) {
        this.type = type;
    }

    /**
     * For DeployVariableKind serialize.
     */
    @JsonCreator
    public DeployVariableKind getByValue(String type) {
        for (DeployVariableKind deployVariableKind : values()) {
            if (deployVariableKind.type.equals(StringUtils.lowerCase(type))) {
                return deployVariableKind;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("DeployVariableKind value %s is not supported.", type));
    }

    /**
     * For DeployVariableKind deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}
