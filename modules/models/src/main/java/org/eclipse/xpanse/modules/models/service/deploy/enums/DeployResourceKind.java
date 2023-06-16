/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.deploy.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.PublicIp;
import org.eclipse.xpanse.modules.models.service.deploy.Vm;
import org.eclipse.xpanse.modules.models.service.deploy.Volume;
import org.eclipse.xpanse.modules.models.service.deploy.Vpc;


/**
 * The kind of the Resources.
 */
public enum DeployResourceKind {
    VM("vm"),
    CONTAINER("container"),
    PUBLIC_IP("publicIP"),
    VPC("vpc"),
    VOLUME("volume"),
    UNKNOWN("unknown");

    private final String kind;

    DeployResourceKind(String kind) {
        this.kind = kind;
    }

    /**
     * get resourceInstance by resourceKind.
     *
     * @param resourceKind deployResourceKind
     * @return resourceInstance
     */
    public static DeployResource getInstanceByKind(DeployResourceKind resourceKind) {
        return switch (resourceKind) {
            case VM -> new Vm();
            case VPC -> new Vpc();
            case VOLUME -> new Volume();
            case PUBLIC_IP -> new PublicIp();
            default -> new DeployResource();
        };
    }

    /**
     * For XpanseResourceKind serialize.
     */
    @JsonCreator
    public DeployResourceKind getByValue(String kind) {
        for (DeployResourceKind resourceKind : values()) {
            if (resourceKind.kind.equals(StringUtils.lowerCase(kind))) {
                return resourceKind;
            }
        }
        return null;
    }

    /**
     * For XpanseResourceKind deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.kind;
    }
}