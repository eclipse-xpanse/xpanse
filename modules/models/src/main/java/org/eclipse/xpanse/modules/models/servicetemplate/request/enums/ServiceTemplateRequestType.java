/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.request.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Defines types of request for managing service template. */
public enum ServiceTemplateRequestType {
    REGISTER("register"),
    UPDATE("update"),
    UNREGISTER("unregister"),
    RE_REGISTER("re-register");

    private final String type;

    ServiceTemplateRequestType(String type) {
        this.type = type;
    }

    /** For ServiceTemplateRequestType deserialize. */
    @JsonCreator
    public static ServiceTemplateRequestType getByValue(String type) {
        for (ServiceTemplateRequestType registrationState : values()) {
            if (StringUtils.equalsIgnoreCase(registrationState.type, type)) {
                return registrationState;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("ServiceTemplateRequestType value %s is not supported.", type));
    }

    /** For ServiceTemplateRequestType serialize. */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}
