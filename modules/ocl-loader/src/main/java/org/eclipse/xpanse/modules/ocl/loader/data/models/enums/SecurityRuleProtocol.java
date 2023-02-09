/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models.enums;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * Protocol types for SecurityRule.
 */
public enum SecurityRuleProtocol {
    TCP("tcp"),
    UDP("udp");

    private final String protocol;

    SecurityRuleProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * For SecurityRuleProtocol serialize.
     */
    @JsonCreator
    public SecurityRuleProtocol getByValue(String protocol) {
        for (SecurityRuleProtocol securityRuleProtocol : values()) {
            if (securityRuleProtocol.protocol.equals(StringUtils.upperCase(protocol))) {
                return securityRuleProtocol;
            }
        }
        return null;
    }

    /**
     * For SecurityRuleProtocol deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.protocol;
    }
}