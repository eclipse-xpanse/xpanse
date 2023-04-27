/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * The credential types.
 */
public enum CredentialType {
    VARIABLES("variables"),
    HTTP_AUTHENTICATION("http_authentication"),
    API_KEY("api_key"),
    OAUTH2("oauth2");

    private final String type;

    CredentialType(String type) {
        this.type = type;
    }

    /**
     * For CredentialType serialize.
     */
    @JsonCreator
    public CredentialType getByValue(String type) {
        for (CredentialType credentialType : values()) {
            if (credentialType.type.equals(StringUtils.lowerCase(type))) {
                return credentialType;
            }
        }
        return null;
    }

    /**
     * For CredentialType deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}
