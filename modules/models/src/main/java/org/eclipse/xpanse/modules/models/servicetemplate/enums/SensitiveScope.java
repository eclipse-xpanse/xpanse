/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Deploy variable sensitive scope. */
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

    /** For SensitiveScope serialize. */
    @JsonCreator
    public SensitiveScope getByValue(String scope) {
        for (SensitiveScope sensitiveScope : values()) {
            if (sensitiveScope.scope.equals(StringUtils.lowerCase(scope))) {
                return sensitiveScope;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("SensitiveScope value %s is not supported.", scope));
    }

    /** For SensitiveScope deserialize. */
    @JsonValue
    public String toValue() {
        return this.scope;
    }
}
