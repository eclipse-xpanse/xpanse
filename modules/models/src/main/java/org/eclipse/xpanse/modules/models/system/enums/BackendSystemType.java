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
 * Defines type of the backend system.
 */
public enum BackendSystemType {

    IDENTITY_PROVIDER("IdentityProvider"),
    DATABASE("Database"),
    TERRAFORM_BOOT("Terraform Boot");

    private final String code;

    BackendSystemType(String code) {
        this.code = code;
    }

    /**
     * For BackendSystemType serialize.
     */
    @JsonCreator
    public static BackendSystemType getByValue(String code) {
        for (BackendSystemType providerType : values()) {
            if (StringUtils.equalsIgnoreCase(code, providerType.code)) {
                return providerType;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("BackendSystemType value %s is not supported.", code));
    }

    /**
     * For BackendSystemType deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.code;
    }
}
