/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.plugin.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;
import org.eclipse.xpanse.modules.plugin.monitor.enums.MetricItemType;

/**
 * Defines metric at a specific timestamp.
 */
@Data
public class MetricItem {

    @Schema(description = "The labels for the MetricItem.")
    public Map<String, String> labels;

    @NotNull
    @Schema(description = "Type of the MetricItem.")
    public MetricItemType type;

    @NotNull
    @Schema(description = "Timestamp of the recorded metric.")
    public Long timeStamp;

    @NotNull
    @Schema(description = "value of the MetricItem.")
    public Number value;
}
