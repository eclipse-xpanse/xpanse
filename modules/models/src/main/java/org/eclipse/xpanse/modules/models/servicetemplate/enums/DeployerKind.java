/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/**
 * Deployer kinds.
 */
public enum DeployerKind {
    TERRAFORM("terraform");

    private final String type;

    DeployerKind(String type) {
        this.type = type;
    }

    /**
     * For DeployerType serialize.
     */
    @JsonCreator
    public DeployerKind getByValue(String type) {
        for (DeployerKind csp : values()) {
            if (csp.type.equals(StringUtils.lowerCase(type))) {
                return csp;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("DeployerKind value %s is not supported.", type));
    }

    /**
     * For DeployerType deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}