/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** The kind of the Resources. */
public enum DeployResourceKind {
    VM("vm", null),
    CONTAINER("container", null),
    PUBLIC_IP("publicIP", null),
    VPC("vpc", null),
    VOLUME("volume", null),
    UNKNOWN("unknown", null),
    SECURITY_GROUP("security_group", DeployResourceKind.VPC),
    SECURITY_GROUP_RULE("security_group_rule", DeployResourceKind.VPC),
    KEYPAIR("keypair", null),
    SUBNET("subnet", DeployResourceKind.VPC);

    private final String kind;

    /** For XpanseResourceKind deserialize. */
    @Getter private final DeployResourceKind parent;

    DeployResourceKind(String kind, DeployResourceKind parent) {
        this.kind = kind;
        this.parent = parent;
    }

    /** For XpanseResourceKind serialize. */
    @JsonCreator
    public static DeployResourceKind getByValue(String kind) {
        for (DeployResourceKind resourceKind : values()) {
            if (StringUtils.endsWithIgnoreCase(resourceKind.kind, kind)) {
                return resourceKind;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("DeployResourceKind value %s is not supported.", kind));
    }

    /** For XpanseResourceKind deserialize. */
    @JsonValue
    public String toValue() {
        return this.kind;
    }
}
