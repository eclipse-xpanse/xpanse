/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.deploy.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/**
 * Enumeration class for service running status.
 */
public enum ServiceState {

    NOT_RUNNING("not running"),

    RUNNING("running"),
    STARTING("starting"),
    STARTING_FAILED("starting failed"),

    STOPPING("stopping"),
    STOPPED("stopped"),
    STOPPING_FAILED("stopping failed");

    private final String state;

    ServiceState(String state) {
        this.state = state;
    }

    /**
     * For ServiceDeploymentState deserialize.
     */
    @JsonCreator
    public static ServiceState getByValue(String state) {
        for (ServiceState serviceState : values()) {
            if (StringUtils.equalsIgnoreCase(serviceState.state, state)) {
                return serviceState;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("ServiceState value %s is not supported.", state));
    }

    /**
     * For ServiceState serialize.
     */
    @JsonValue
    public String toValue() {
        return this.state;
    }

}