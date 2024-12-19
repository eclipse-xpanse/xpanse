/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.request.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Defines status enumerations for request of service template. */
public enum ServiceTemplateRequestStatus {
    IN_REVIEW("in-review"),
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    CANCELED("canceled");

    private final String state;

    ServiceTemplateRequestStatus(String state) {
        this.state = state;
    }

    /** For ServiceTemplateRequestStatus deserialize. */
    @JsonCreator
    public static ServiceTemplateRequestStatus getByValue(String state) {
        for (ServiceTemplateRequestStatus registrationState : values()) {
            if (StringUtils.equalsIgnoreCase(registrationState.state, state)) {
                return registrationState;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("ServiceTemplateRequestStatus value %s is not supported.", state));
    }

    /** For ServiceTemplateRequestStatus serialize. */
    @JsonValue
    public String toValue() {
        return this.state;
    }
}
