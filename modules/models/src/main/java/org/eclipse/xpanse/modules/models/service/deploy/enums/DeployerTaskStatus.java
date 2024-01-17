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
 * Deployment state. This is the internal state of every task invoked by the deployer.
 * This state is not exposed to the end user.
 */
public enum DeployerTaskStatus {
    INIT("initial"),
    DEPLOY_SUCCESS("success"),
    DEPLOY_FAILED("failed"),
    DESTROY_SUCCESS("destroy_success"),
    DESTROY_FAILED("destroy_failed");

    private final String status;

    DeployerTaskStatus(String status) {
        this.status = status;
    }

    /**
     * For DeployerTaskStatus deserialize.
     */
    @JsonCreator
    public DeployerTaskStatus getByValue(String period) {
        for (DeployerTaskStatus xpanseDeployStatus : values()) {
            if (xpanseDeployStatus.status.equals(StringUtils.lowerCase(period))) {
                return xpanseDeployStatus;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("DeployerTaskStatus value %s is not supported.", period));
    }

    /**
     * For DeployerTaskStatus serialize.
     */
    @JsonValue
    public String toValue() {
        return this.status;
    }
}
