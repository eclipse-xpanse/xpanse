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

/** ServiceConfigurationUpdate status. */
public enum ServiceConfigurationStatus {
    PENDING("pending"),
    PROCESSING("processing"),
    SUCCESSFUL("successful"),
    ERROR("error");

    private final String status;

    ServiceConfigurationStatus(String status) {
        this.status = status;
    }

    /** For ServiceConfigurationStatus serialize. */
    @JsonCreator
    public static ServiceConfigurationStatus getByValue(String status) {
        for (ServiceConfigurationStatus serviceConfigurationStatus : values()) {
            if (StringUtils.equalsIgnoreCase(serviceConfigurationStatus.status, status)) {
                return serviceConfigurationStatus;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("ServiceConfigurationStatus value %s is not supported.", status));
    }

    /** For ServiceConfigurationStatus deserialize. */
    @JsonValue
    public String toValue() {
        return this.status;
    }
}
