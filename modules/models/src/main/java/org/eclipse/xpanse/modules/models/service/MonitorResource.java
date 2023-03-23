/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service;

import java.util.List;
import lombok.Data;

/**
 * MonitorResource model.
 */
@Data
public class MonitorResource {

    /**
     * The cpu of the cpuUsage.
     */
    private List<MonitorDataResponse> cpu;

    /**
     * The mem of the memUsage.
     */
    private List<MonitorDataResponse> mem;

}
