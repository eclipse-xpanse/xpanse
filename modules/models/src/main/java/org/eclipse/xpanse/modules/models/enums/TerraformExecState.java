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
 * Deployment state.
 */
public enum TerraformExecState {
    INIT("initial"),
    DEPLOY_SUCCESS("success"),
    DEPLOY_FAILED("failed"),
    DESTROY_SUCCESS("destroy_success"),
    DESTROY_FAILED("destroy_failed");

    private final String status;

    TerraformExecState(String status) {
        this.status = status;
    }

    /**
     * For XpanseDeployStatus serialize.
     */
    @JsonCreator
    public TerraformExecState getByValue(String period) {
        for (TerraformExecState xpanseDeployStatus : values()) {
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
