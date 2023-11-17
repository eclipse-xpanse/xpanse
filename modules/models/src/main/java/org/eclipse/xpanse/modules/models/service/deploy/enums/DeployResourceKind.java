/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;


/**
 * The kind of the Resources.
 */
public enum DeployResourceKind {
    VM("vm"),
    CONTAINER("container"),
    PUBLIC_IP("publicIP"),
    VPC("vpc"),
    VOLUME("volume"),
    UNKNOWN("unknown"),
    SECURITY_GROUP("security_group"),
    SECURITY_GROUP_RULE("security_group_rule"),
    KEYPAIR("keypair"),
    SUBNET("subnet");

    private final String kind;

    DeployResourceKind(String kind) {
        this.kind = kind;
    }

    /**
     * For XpanseResourceKind serialize.
     */
    @JsonCreator
    public DeployResourceKind getByValue(String kind) {
        for (DeployResourceKind resourceKind : values()) {
            if (StringUtils.endsWithIgnoreCase(resourceKind.kind, kind)) {
                return resourceKind;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("DeployResourceKind value %s is not supported.", kind));
    }

    /**
     * For XpanseResourceKind deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.kind;
    }
}