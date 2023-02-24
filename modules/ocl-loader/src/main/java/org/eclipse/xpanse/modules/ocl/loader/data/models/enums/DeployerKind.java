/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Deployer kinds.
 */
public enum DeployerKind {
    TERRAFORM("terraform"),
    PULUMI("pulumi");

    private final String type;

    DeployerKind(String type) {
        this.type = type;
    }

    /**
     * For DeployerType serialize.
     */
    @JsonCreator
    public DeployerKind getByValue(String type) {
        for (DeployerKind csp : values()) {
            if (csp.type.equals(type)) {
                return csp;
            }
        }
        return null;
    }

    /**
     * For DeployerType deserialize.
     */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}