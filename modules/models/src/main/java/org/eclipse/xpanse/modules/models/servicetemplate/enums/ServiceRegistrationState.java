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
 * Defines possible states of a managed service registration.
 */
public enum ServiceRegistrationState {

    UNREGISTERED("unregistered"),
    APPROVAL_PENDING("approval pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    private final String state;

    ServiceRegistrationState(String state) {
        this.state = state;
    }

    /**
     * For ServiceRegistrationState deserialize.
     */
    @JsonCreator
    public static ServiceRegistrationState getByValue(String state) {
        for (ServiceRegistrationState serviceState : values()) {
            if (serviceState.state.equals(StringUtils.lowerCase(state))) {
                return serviceState;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("ServiceRegistrationState value %s is not supported.", state));
    }

    /**
     * For ServiceRegistrationState serialize.
     */
    @JsonValue
    public String toValue() {
        return this.state;
    }
}

