/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.monitor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/** FlexibleEngine Namespace. */
public enum FlexibleEngineNameSpaceKind {
    ECS_SYS("SYS.ECS"),
    ECS_AGT("AGT.ECS");

    private final String nameSpace;

    FlexibleEngineNameSpaceKind(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    /** For FlexibleEngineResource deserialize. */
    @JsonValue
    public String toValue() {
        return this.nameSpace;
    }

    /** For FlexibleEngineResource serialize. */
    @JsonCreator
    public FlexibleEngineNameSpaceKind getByValue(String nameSpace) {
        for (FlexibleEngineNameSpaceKind flexibleEngineNameSpaceKind : values()) {
            if (StringUtils.equalsIgnoreCase(flexibleEngineNameSpaceKind.nameSpace, nameSpace)) {
                return flexibleEngineNameSpaceKind;
            }
        }
        return null;
    }
}
