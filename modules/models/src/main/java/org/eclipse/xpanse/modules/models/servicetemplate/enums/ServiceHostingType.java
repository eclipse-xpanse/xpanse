/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Defines which cloud service account is used for deploying cloud resources. */
@Schema(description = "Defines which cloud service account is used for deploying cloud resources.")
public enum ServiceHostingType {
    SELF("self"),
    SERVICE_VENDOR("service-vendor");

    private final String serviceHostingType;

    ServiceHostingType(String serviceHostingType) {
        this.serviceHostingType = serviceHostingType;
    }

    /** For ServiceHostingType serialize. */
    @JsonCreator
    public static ServiceHostingType getByValue(String type) {
        for (ServiceHostingType serviceHostingType : values()) {
            if (serviceHostingType.toValue().equals(StringUtils.lowerCase(type))) {
                return serviceHostingType;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("ServiceHostingType value %s is not supported.", type));
    }

    /** For ServiceHostingType deserialize. */
    @JsonValue
    public String toValue() {
        return this.serviceHostingType;
    }
}
