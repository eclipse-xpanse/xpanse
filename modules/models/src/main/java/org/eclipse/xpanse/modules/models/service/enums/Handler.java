/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** The kind of the service order handler. */
public enum Handler {
    TERRAFORM_LOCAL("terraform-local"),
    TERRAFORM_BOOT("terraform-boot"),
    OPEN_TOFU_LOCAL("open-tofu-local"),
    TOFU_MAKER("tofu-maker"),
    INTERNAL("internal"), // lock/unlock and any other orders
    WORKFLOW("workFlow"), // port, recreate
    AGENT("agent"), // config change
    PLUGIN("plugin"); // restart, stop and start

    private final String value;

    Handler(String value) {
        this.value = value;
    }

    /** For Handler deserialize. */
    @JsonCreator
    public static Handler getByValue(String name) {
        for (Handler handler : values()) {
            if (StringUtils.equalsIgnoreCase(handler.value, name)) {
                return handler;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("Handler value %s is not supported.", name));
    }

    /** For Handler serialize. */
    @JsonValue
    public String toValue() {
        return this.value;
    }
}
