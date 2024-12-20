/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Defines possible states of a managed service template registration. */
public enum ServiceTemplateRegistrationState {
    IN_REVIEW("in-review"),
    APPROVED("approved"),
    CANCELLED("cancelled"),
    REJECTED("rejected");

    private final String state;

    ServiceTemplateRegistrationState(String state) {
        this.state = state;
    }

    /** For ServiceTemplateRegistrationState deserialize. */
    @JsonCreator
    public static ServiceTemplateRegistrationState getByValue(String state) {
        for (ServiceTemplateRegistrationState registrationState : values()) {
            if (StringUtils.equalsIgnoreCase(registrationState.state, state)) {
                return registrationState;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format(
                        "ServiceTemplateRegistrationState value %s is not supported.", state));
    }

    /** For ServiceTemplateRegistrationState serialize. */
    @JsonValue
    public String toValue() {
        return this.state;
    }
}
