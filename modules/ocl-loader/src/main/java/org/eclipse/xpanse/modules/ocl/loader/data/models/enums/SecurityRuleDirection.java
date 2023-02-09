/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * Direction types for SecurityRule.
 */
public enum SecurityRuleDirection {
    IN("in"),
    OUT("out");

    private final String direction;

    SecurityRuleDirection(String direction) {
        this.direction = direction;
    }

    /**
     * For SecurityRuleDirection serialize.
     */
    @JsonCreator
    public SecurityRuleDirection getByValue(String direction) {
        for (SecurityRuleDirection securityRuleDirection : values()) {
            if (securityRuleDirection.direction.equals(StringUtils.lowerCase(direction))) {
                return securityRuleDirection;
            }
        }
        return null;
    }

    /**
     * For SecurityRuleDirection deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.direction;
    }
}