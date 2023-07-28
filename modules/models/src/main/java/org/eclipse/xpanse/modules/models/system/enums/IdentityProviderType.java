/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.system.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

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
        throw new UnsupportedEnumValueException(
                String.format("IdentityProviderType value %s is not supported.", code));
    }

    /**
     * For IdentityProviderType deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.code;
    }
}
