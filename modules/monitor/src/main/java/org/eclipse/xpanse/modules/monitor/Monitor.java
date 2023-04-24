/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor;

import java.util.List;

/**
 * The Monitor of the services.
 */
public class Monitor {

    /**
     * Get metrics of the service instance.
     *
     * @param serviceInstance The deployed service instance.
     */
    List<Metric> getMetrics(ServiceInstance serviceInstance) {
        return null;
    }
}
