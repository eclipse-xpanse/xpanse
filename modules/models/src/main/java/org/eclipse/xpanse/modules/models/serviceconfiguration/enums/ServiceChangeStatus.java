/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.serviceconfiguration.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Service Change Status. */
public enum ServiceChangeStatus {
    PENDING("pending"),
    PROCESSING("processing"),
    SUCCESSFUL("successful"),
    ERROR("error");

    private final String status;

    ServiceChangeStatus(String status) {
        this.status = status;
    }

    /** For ServiceChangeStatus serialize. */
    @JsonCreator
    public static ServiceChangeStatus getByValue(String status) {
        for (ServiceChangeStatus serviceChangeStatus : values()) {
            if (StringUtils.equalsIgnoreCase(serviceChangeStatus.status, status)) {
                return serviceChangeStatus;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("ServiceChangeStatus value %s is not supported.", status));
    }

    /** For ServiceChangeStatus deserialize. */
    @JsonValue
    public String toValue() {
        return this.status;
    }
}
