/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.enums;

import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.models.service.Disk;
import org.eclipse.xpanse.modules.models.service.Vm;

/**
 * Defines deployResourceKind and deployResourceInstance.
 */
public enum ResourceKindInstance {

    VM_INSTANCE(DeployResourceKind.VM, new Vm()),
    HUAWEI_DISK_PROPERTY(DeployResourceKind.DISK, new Disk());


    private final DeployResourceKind resourceKind;
    private final DeployResource resourceInstance;

    ResourceKindInstance(DeployResourceKind resourceKind,
            DeployResource resourceInstance) {
        this.resourceKind = resourceKind;
        this.resourceInstance = resourceInstance;
    }

    /**
     * get resourceInstance by resourceKind.
     *
     * @param resourceKind deployResourceKind
     * @return resourceInstance
     */
    public static DeployResource getInstance(DeployResourceKind resourceKind) {
        for (ResourceKindInstance enumItem : values()) {
            if (enumItem.resourceKind.equals(resourceKind)) {
                return enumItem.resourceInstance;
            }
        }
        return new DeployResource();
    }
}
