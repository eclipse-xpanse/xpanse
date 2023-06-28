/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.register.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * Defines possible states of a managed service registration.
 */
public enum ServiceRegistrationState {

    REGISTERED("registered"),
    UPDATED("updated");

    private final String state;

    ServiceRegistrationState(String state) {
        this.state = state;
    }

    /**
     * For ServiceRegistrationState deserialize.
     */
    @JsonCreator
    public ServiceRegistrationState getByValue(String state) {
        for (ServiceRegistrationState serviceState : values()) {
            if (serviceState.state.equals(StringUtils.lowerCase(state))) {
                return serviceState;
            }
        }
        return null;
    }

    /**
     * For ServiceRegistrationState serialize.
     */
    @JsonValue
    public String toValue() {
        return this.name();
    }
}

