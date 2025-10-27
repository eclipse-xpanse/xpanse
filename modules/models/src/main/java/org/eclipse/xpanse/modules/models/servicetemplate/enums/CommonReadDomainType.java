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
 * Enum to describe common domain of xpanse does the read method refer to. It includes only domains
 * which does not have a specific place in the OCL.
 */
public enum CommonReadDomainType {
    ORDERS("orders"),
    SERVICES("services"),
    WORKFLOWS("workflows");

    private final String type;

    CommonReadDomainType(String type) {
        this.type = type;
    }

    /** For CommonReadRequestType serialize. */
    @JsonCreator
    public static CommonReadDomainType getByValue(String type) {
        for (CommonReadDomainType tool : values()) {
            if (StringUtils.equalsIgnoreCase(tool.type, type)) {
                return tool;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("CommonReadRequestType value %s is not supported.", type));
    }

    /** For CommonReadRequestType deserialize. */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}
