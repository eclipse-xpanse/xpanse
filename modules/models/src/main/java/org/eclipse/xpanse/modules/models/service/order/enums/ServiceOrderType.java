/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.order.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Enumeration class for types of service order tasks. */
public enum ServiceOrderType {
    DEPLOY("deploy"),
    RETRY("retry"),
    ROLLBACK("rollback"),
    MODIFY("modify"),
    DESTROY("destroy"),
    PORT("port"),
    RECREATE("recreate"),
    LOCK_CHANGE("lockChange"),
    CONFIG_CHANGE("configChange"),
    SERVICE_ACTION("serviceAction"),
    PURGE("purge"),
    SERVICE_START("serviceStart"),
    SERVICE_STOP("serviceStop"),
    SERVICE_RESTART("serviceRestart"),
    OBJECT_CREATE("objectCreate"),
    OBJECT_MODIFY("objectModify"),
    OBJECT_DELETE("objectDelete");

    private final String type;

    ServiceOrderType(String type) {
        this.type = type;
    }

    /** For ServiceOrderType deserialize. */
    @JsonCreator
    public static ServiceOrderType getByValue(String type) {
        for (ServiceOrderType item : values()) {
            if (StringUtils.equalsIgnoreCase(type, item.type)) {
                return item;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("ServiceOrderType type %s is not supported.", type));
    }

    /** For ServiceOrderType serialize. */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}
