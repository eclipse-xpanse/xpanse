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
 * State for resources in runtime.
 */
public enum RuntimeState {
    ACTIVE("active"),
    INACTIVE("inactive"),

    BUILDING("building");

    private final String state;

    RuntimeState(String state) {
        this.state = state;
    }

    /**
     * For RuntimeState serialize.
     */
    @JsonCreator
    public RuntimeState getByValue(String state) {
        for (RuntimeState runtimeState : values()) {
            if (runtimeState.state.equals(StringUtils.lowerCase(state))) {
                return runtimeState;
            }
        }
        return null;
    }

    /**
     * For RuntimeState deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.state;
    }
}