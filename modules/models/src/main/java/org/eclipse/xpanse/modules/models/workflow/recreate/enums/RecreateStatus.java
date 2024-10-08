/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.workflow.recreate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/**
 * Defines possible states of a service recreate request.
 */
public enum RecreateStatus {

    RECREATE_STARTED("RecreateStarted"),
    RECREATE_COMPLETED("RecreateCompleted"),
    RECREATE_FAILED("RecreateFailed"),

    DESTROY_STARTED("DestroyStarted"),
    DESTROY_FAILED("DestroyFailed"),
    DESTROY_COMPLETED("DestroyCompleted"),

    DEPLOY_STARTED("DeployStarted"),
    DEPLOY_FAILED("DeployFailed"),
    DEPLOY_COMPLETED("DeployCompleted");


    private final String state;

    RecreateStatus(String state) {
        this.state = state;
    }

    /**
     * For RecreateStatus deserialize.
     */
    @JsonCreator
    public static RecreateStatus getByValue(String state) {
        for (RecreateStatus recreateStatus : values()) {
            if (StringUtils.equalsIgnoreCase(recreateStatus.state, state)) {
                return recreateStatus;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("RecreateStatus value %s is not supported.", state));
    }

    /**
     * For RecreateStatus serialize.
     */
    @JsonValue
    public String toValue() {
        return this.state;
    }

}
