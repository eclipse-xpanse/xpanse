/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/**
 * Fields that can be mapped to service specific values. These values will be replaced in the
 * generated in teh controller schema.
 */
public enum MappableFields {
    SERVICE_ID("serviceId"),
    OBJECT_ID("objectId"),
    ORDER_ID("orderId");

    private final String field;

    MappableFields(String field) {
        this.field = field;
    }

    /** Convert strings to enum values. */
    @JsonCreator
    public static MappableFields getByValue(String field) {
        for (MappableFields mappableFields : values()) {
            if (StringUtils.equalsIgnoreCase(mappableFields.field, field)) {
                return mappableFields;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("DeployerKind value %s is not supported.", field));
    }

    /** For DeployerType deserialize. */
    @JsonValue
    public String toValue() {
        return this.field;
    }
}
