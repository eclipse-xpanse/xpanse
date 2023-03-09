/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * Defines possible states of a managed service.
 */
public enum ServiceState {

    REGISTERED("registered"),
    UPDATED("updated"),
    DEPLOYING("deploying"),
    DEPLOY_SUCCESS("deploy_success"),
    DEPLOY_FAILED("deploy_failed"),
    DESTROYING("destroying"),
    DESTROY_SUCCESS("destroy_success"),
    DESTROY_FAILED("destroy_failed");


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
        return this.name();
    }
}
