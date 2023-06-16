/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.plugin.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.eclipse.xpanse.modules.plugin.monitor.enums.MetricType;
import org.eclipse.xpanse.modules.plugin.monitor.enums.MetricUnit;

/**
 * One kind of metric.
 */
@Data
public class Metric {

    @NotNull
    @Schema(description = "The name of the metric.")
    String name;

    @Schema(description = "The description of the metric.")
    String description;

    @NotNull
    @Schema(description = "The type of the metric.")
    MetricType type;

    @NotNull
    @Schema(description = "The unit of the metric.")
    MetricUnit unit;

    @Schema(description = "The labels of the metric.")
    Map<String, String> labels;

    @Schema(description = "The list of the metric items.")
    List<MetricItem> metrics;

}
