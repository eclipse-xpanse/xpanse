/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.system.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * Defines type of identity provider.
 */
public enum IdentityProviderType {

    ZITADEL("zitadel");

    private final String code;

    IdentityProviderType(String code) {
        this.code = code;
    }

    /**
     * For IdentityProviderType serialize.
     */
    @JsonCreator
    public static IdentityProviderType getByValue(String code) {
        for (IdentityProviderType providerType : values()) {
            if (StringUtils.equalsIgnoreCase(code, providerType.code)) {
                return providerType;
            }
        }
        return null;
    }

    /**
     * For IdentityProviderType deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.code;
    }
}
