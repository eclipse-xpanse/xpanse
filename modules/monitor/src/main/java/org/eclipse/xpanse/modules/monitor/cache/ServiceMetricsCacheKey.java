/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.monitor.cache;


import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;

/**
 * Defines resource metrics cache key.
 */
public record ServiceMetricsCacheKey(Csp csp,
                                     String resourceId,
                                     MonitorResourceType monitorResourceType) {

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServiceMetricsCacheKey key) {
            return key.csp == this.csp
                    && key.resourceId.equals(this.resourceId)
                    && key.monitorResourceType == this.monitorResourceType;
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((csp == null) ? 0 : csp.hashCode());
        result = prime * result + ((resourceId == null) ? 0 : resourceId.hashCode());
        result = prime * result
                + ((monitorResourceType == null) ? 0 : monitorResourceType.hashCode());
        return result;
    }
}
