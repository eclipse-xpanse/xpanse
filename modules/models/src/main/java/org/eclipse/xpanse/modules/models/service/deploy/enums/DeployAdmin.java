/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.deploy.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * DeployAdmin user.
 */
public enum DeployAdmin {
    CSP("csp"),
    ISV("isv"),
    USER("user");

    private final String type;

    DeployAdmin(String type) {
        this.type = type;
    }

    /**
     * For DeployAdmin serialize.
     */
    @JsonCreator
    public DeployAdmin getByValue(String type) {
        for (DeployAdmin csp : values()) {
            if (csp.type.equals(StringUtils.lowerCase(type))) {
                return csp;
            }
        }
        return null;
    }

    /**
     * For DeployAdmin deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}
