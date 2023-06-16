/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.register.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * Deploy variable sensitive scope.
 */
public enum SensitiveScope {
    // Not sensitive.
    NONE("none"),
    // Only sensitive in one request, no need to be stored.
    ONCE("once"),
    // Keep sensitive in all the cycle life.
    ALWAYS("always");

    private final String scope;

    SensitiveScope(String scope) {
        this.scope = scope;
    }

    /**
     * For SensitiveScope serialize.
     */
    @JsonCreator
    public SensitiveScope getByValue(String scope) {
        for (SensitiveScope sensitiveScope : values()) {
            if (sensitiveScope.scope.equals(StringUtils.lowerCase(scope))) {
                return sensitiveScope;
            }
        }
        return null;
    }

    /**
     * For SensitiveScope deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.scope;
    }
}
