/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * Deploy variable kinds.
 * fix_env: This kind of variables must be settled in the OCL template,
 *   and will be injected to the deployment as environment variables.
 * fix_variable: This kind of variables must be settled in the OCL template,
 *   and will be injected to the deployment as variables.
 * env: This kind of variables can be settled by the API or the xpanse
 *   runtime environment (The API takes higher priority), and will be injected
 *   to the deployment as environment variables.
 * variable: This kind of variables can be settled by the API or the xpanse
 *   runtime environment (The API takes higher priority), and will be injected
 *   to the deployment as variables.
 * env_env: This kind of variables must be settled by the xpanse runtime environment,
 *   and will be injected to the deployment as environment variables.
 * env_variable: This kind of variables must be settled by the xpanse runtime
 *   environment, and will be injected to the deployment as variables.
 */
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
        return null;
    }

    /**
     * For DeployVariableKind deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}
