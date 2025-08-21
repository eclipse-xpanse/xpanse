/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.monitor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;
import org.eclipse.xpanse.modules.models.monitor.enums.MetricItemType;

/** Defines metric at a specific timestamp. */
@Data
public class MetricItem {

    @Schema(description = "The labels for the MetricItem.")
    private Map<String, String> labels;

    @NotNull
    @Schema(description = "Type of the MetricItem.")
    private MetricItemType type;

    @NotNull
    @Schema(description = "Timestamp of the recorded metric.")
    private Long timeStamp;

    @NotNull
    @Schema(description = "value of the MetricItem.")
    private Number value;
}
