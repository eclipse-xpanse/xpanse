/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/**
 * Cloud service providers.
 */
public enum Csp {
    HUAWEI_CLOUD("HuaweiCloud"),
    FLEXIBLE_ENGINE("FlexibleEngine"),
    OPENSTACK_TESTLAB("OpenstackTestlab"),
    PLUS_SERVER("PlusServer"),
    REGIO_CLOUD("RegioCloud"),
    ALIBABA_CLOUD("AlibabaCloud"),
    AWS("aws"),
    AZURE("azure"),
    GCP("GoogleCloudPlatform");

    private final String value;

    Csp(String value) {
        this.value = value;
    }

    /**
     * For CSP deserialize.
     */
    @JsonCreator
    public static Csp getByValue(String name) {
        for (Csp csp : values()) {
            if (StringUtils.equalsIgnoreCase(csp.value, name)) {
                return csp;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("Csp value %s is not supported.", name));
    }

    /**
     * For CSP serialize.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }
}
