/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * Defines possible states of a managed service.
 */
public enum ServiceState {

    REGISTERED("registered"),
    STARTED("started"),
    FAILED("failed"),
    DELETING("deleting"),
    STOPPED("stopped"),
    UPDATING("updating"),
    UPDATED("updated"),
    STARTING("starting");

    private final String serviceState;

    ServiceState(String serviceState) {
        this.serviceState = serviceState;
    }

    /**
     * For BillingPeriod serialize.
     */
    @JsonCreator
    public ServiceState getByValue(String state) {
        for (ServiceState serviceState : values()) {
            if (serviceState.serviceState.equals(StringUtils.lowerCase(state))) {
                return serviceState;
            }
        }
        return null;
    }

    /**
     * For BillingPeriod deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.serviceState;
    }
}
