/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Defines review result for service template registration. */
public enum ServiceReviewResult {
    APPROVED("approved"),
    REJECTED("rejected");

    private final String result;

    ServiceReviewResult(String result) {
        this.result = result;
    }

    /** For ServiceReviewResult deserialize. */
    @JsonCreator
    public static ServiceReviewResult getByValue(String result) {
        for (ServiceReviewResult serviceState : values()) {
            if (serviceState.result.equals(StringUtils.lowerCase(result))) {
                return serviceState;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("ServiceReviewResult value %s is not supported.", result));
    }

    /** For ServiceReviewResult serialize. */
    @JsonValue
    public String toValue() {
        return this.result;
    }
}
