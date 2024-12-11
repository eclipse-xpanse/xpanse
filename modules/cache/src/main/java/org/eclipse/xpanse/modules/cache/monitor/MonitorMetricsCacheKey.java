/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.cache.monitor;

import java.io.Serial;
import java.io.Serializable;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;

/** Defines resource monitor metrics cache key. */
public record MonitorMetricsCacheKey(
        Csp csp, String monitorResourceId, MonitorResourceType monitorResourceType)
        implements Serializable {

    @Serial private static final long serialVersionUID = 5713740900243186437L;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MonitorMetricsCacheKey key) {
            return key.csp == this.csp
                    && key.monitorResourceId.equals(this.monitorResourceId)
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
        result = prime * result + ((monitorResourceId == null) ? 0 : monitorResourceId.hashCode());
        result =
                prime * result
                        + ((monitorResourceType == null) ? 0 : monitorResourceType.hashCode());
        return result;
    }
}
