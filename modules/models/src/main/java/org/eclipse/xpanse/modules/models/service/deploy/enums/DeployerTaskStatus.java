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
 * Deployer task state. This is the internal state of every task invoked by the deployer.
 * This state is not exposed to the end user.
 */
public enum DeployerTaskStatus {
    INIT("initial"),
    DEPLOY_SUCCESS("success"),
    DEPLOY_FAILED("failed"),
    DESTROY_SUCCESS("destroy_success"),
    DESTROY_FAILED("destroy_failed"),
    ROLLBACK_SUCCESS("rollback_success"),
    ROLLBACK_FAILED("rollback_failed"),
    PURGE_SUCCESS("purge_success"),
    PURGE_FAILED("purge_failed");

    private final String status;

    DeployerTaskStatus(String status) {
        this.status = status;
    }

    /**
     * For DeployerTaskStatus deserialize.
     */
    @JsonCreator
    public static DeployerTaskStatus getByValue(String status) {
        for (DeployerTaskStatus deployerTaskStatus : values()) {
            if (StringUtils.equalsIgnoreCase(status, deployerTaskStatus.status)) {
                return deployerTaskStatus;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("DeployerTaskStatus value %s is not supported.", status));
    }

    /**
     * For DeployerTaskStatus serialize.
     */
    @JsonValue
    public String toValue() {
        return this.status;
    }
}
