/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;

/** Defines the type of the kubernetes cluster on which the service will be deployed. */
public enum KubernetesClusterType {
    DEDICATED("dedicated"),
    SHARED("shared");

    private final String type;

    KubernetesClusterType(String type) {
        this.type = type;
    }

    /** For KubernetesClusterType serialize. */
    @JsonCreator
    public static KubernetesClusterType getByValue(String type) {
        for (KubernetesClusterType tool : values()) {
            if (StringUtils.equalsIgnoreCase(tool.type, type)) {
                return tool;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("KubernetesClusterType value %s is not supported.", type));
    }

    /** For KubernetesClusterType deserialize. */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}
