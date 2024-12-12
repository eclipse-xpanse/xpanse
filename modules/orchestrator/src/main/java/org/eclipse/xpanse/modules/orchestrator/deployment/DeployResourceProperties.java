/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.deployment;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;

/** Defines methods to get kind and properties for deploy resources. */
public class DeployResourceProperties {

    /**
     * Get kind of deployed resource.
     *
     * @return DeployResourceKind.
     */
    public DeployResourceKind getResourceKind() {
        return DeployResourceKind.UNKNOWN;
    }

    /**
     * Get properties map of resource.
     *
     * @return properties map.
     */
    public Map<String, String> getResourceProperties() {
        return new HashMap<>();
    }
}
