/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud.models;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.xpanse.modules.models.enums.DeployResourceKind;


/**
 * Enum for DeployResourceKind and Huawei Resource Property.
 */
public enum HuaweiResourceProperty {
    HUAWEI_VM_PROPERTY(DeployResourceKind.VM, new HuaweiVmProperty()),
    HUAWEI_DISK_PROPERTY(DeployResourceKind.DISK, new HuaweiDiskProperty()),
    HUAWEI_VPC_PROPERTY(DeployResourceKind.VPC, new HuaweiVpcProperty()),
    HUAWEI_PUBLICIP_PROPERTY(DeployResourceKind.PUBLICIP, new HuaweiPublicIpProperty());

    private final DeployResourceKind resourceKind;
    private final Map<String, String> properties;

    HuaweiResourceProperty(DeployResourceKind resourceKind,
            Map<String, String> resourceProperties) {
        this.resourceKind = resourceKind;
        this.properties = resourceProperties;
    }

    /**
     * get property by resourceKind.
     *
     * @param resourceKind deployResourceKind
     * @return property map
     */
    public static Map<String, String> getProperties(DeployResourceKind resourceKind) {
        for (HuaweiResourceProperty property : values()) {
            if (property.resourceKind.equals(resourceKind)) {
                return property.properties;
            }
        }
        return new HashMap<>();
    }
}