/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.change.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/**
 * Defines status enumerations for change of service template.
 */
public enum ServiceTemplateChangeStatus {

    IN_REVIEW("in-review"),
    ACCEPTED("accepted"),
    REJECTED("rejected");

    private final String state;

    ServiceTemplateChangeStatus(String state) {
        this.state = state;
    }

    /**
     * For ServiceTemplateChangeStatus deserialize.
     */
    @JsonCreator
    public static ServiceTemplateChangeStatus getByValue(String state) {
        for (ServiceTemplateChangeStatus registrationState : values()) {
            if (StringUtils.equalsIgnoreCase(registrationState.state, state)) {
                return registrationState;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("ServiceTemplateChangeStatus value %s is not supported.",
                        state));
    }

    /**
     * For ServiceTemplateChangeStatus serialize.
     */
    @JsonValue
    public String toValue() {
        return this.state;
    }
}

