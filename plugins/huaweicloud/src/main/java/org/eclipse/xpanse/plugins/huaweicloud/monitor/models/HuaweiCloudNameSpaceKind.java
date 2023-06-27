/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.monitor.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * Huawei Namespace.
 */
public enum HuaweiCloudNameSpaceKind {

    ECS_SYS("SYS.ECS"),
    ECS_AGT("AGT.ECS");

    private final String nameSpace;

    HuaweiCloudNameSpaceKind(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    /**
     * For HuaweiResource deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.nameSpace;
    }

    /**
     * For HuaweiResource serialize.
     */
    @JsonCreator
    public HuaweiCloudNameSpaceKind getByValue(String nameSpace) {
        for (HuaweiCloudNameSpaceKind huaweiCloudNameSpaceKind : values()) {
            if (StringUtils.equalsIgnoreCase(huaweiCloudNameSpaceKind.nameSpace, nameSpace)) {
                return huaweiCloudNameSpaceKind;
            }
        }
        return null;
    }
}
