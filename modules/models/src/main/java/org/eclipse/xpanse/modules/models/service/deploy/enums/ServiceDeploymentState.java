/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/**
 * Defines possible states of a managed service.
 */
public enum ServiceDeploymentState {

    DEPLOYING("deploying"),
    DEPLOY_SUCCESS("deployment successful"),
    DEPLOY_FAILED("deployment failed"),
    DESTROYING("destroying"),
    DESTROY_SUCCESS("destroy successful"),
    DESTROY_FAILED("destroy failed"),
    MANUAL_CLEANUP_REQUIRED("manual cleanup required");


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
        throw new UnsupportedEnumValueException(
                String.format("ServiceDeploymentState value %s is not supported.", state));
    }

    /**
     * For ServiceDeploymentState serialize.
     */
    @JsonValue
    public String toValue() {
        return this.state;
    }
}
