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
 * Action types for SecurityRule.
 */
public enum SecurityRuleAction {
    ALLOW("allow"),
    DENY("deny");

    private final String action;

    SecurityRuleAction(String action) {
        this.action = action;
    }

    /**
     * For SecurityRuleAction serialize.
     */
    @JsonCreator
    public SecurityRuleAction getByValue(String action) {
        for (SecurityRuleAction securityRuleAction : values()) {
            if (securityRuleAction.action.equals(StringUtils.lowerCase(action))) {
                return securityRuleAction;
            }
        }
        return null;
    }

    /**
     * For SecurityRuleAction deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.action;
    }
}
