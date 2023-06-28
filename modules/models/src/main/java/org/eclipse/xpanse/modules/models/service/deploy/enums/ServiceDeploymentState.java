/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * Defines possible states of a managed service.
 */
public enum ServiceDeploymentState {

    DEPLOYING("deploying"),
    DEPLOY_SUCCESS("deploy_success"),
    DEPLOY_FAILED("deploy_failed"),
    DESTROYING("destroying"),
    DESTROY_SUCCESS("destroy_success"),
    DESTROY_FAILED("destroy_failed");


    private final String state;

    ServiceDeploymentState(String state) {
        this.state = state;
    }

    /**
     * For ServiceDeploymentState deserialize.
     */
    @JsonCreator
    public ServiceDeploymentState getByValue(String state) {
        for (ServiceDeploymentState serviceState : values()) {
            if (serviceState.state.equals(StringUtils.lowerCase(state))) {
                return serviceState;
            }
        }
        return null;
    }

    /**
     * For ServiceDeploymentState serialize.
     */
    @JsonValue
    public String toValue() {
        return this.name();
    }
}
