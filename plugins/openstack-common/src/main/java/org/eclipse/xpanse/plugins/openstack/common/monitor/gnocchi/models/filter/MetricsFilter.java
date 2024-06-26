/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.common.monitor.gnocchi.models.filter;

import lombok.Builder;
import lombok.Data;

/**
 * Gnocchi object for defining the query parameters to filter metrics.
 */
@Data
@Builder
public class MetricsFilter {

    private Long start;
    private Long end;
    private Integer granularity;
}
