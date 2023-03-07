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
 * Deployment state.
 */
public enum DeployState {
    INIT("initial"),
    SUCCESS("success"),
    FAILED("failed");

    private final String status;

    DeployState(String status) {
        this.status = status;
    }

    /**
     * For XpanseDeployStatus serialize.
     */
    @JsonCreator
    public DeployState getByValue(String period) {
        for (DeployState xpanseDeployStatus : values()) {
            if (xpanseDeployStatus.status.equals(StringUtils.lowerCase(period))) {
                return xpanseDeployStatus;
            }
        }
        return null;
    }

    /**
     * For XpanseDeployStatus deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.status;
    }
}
