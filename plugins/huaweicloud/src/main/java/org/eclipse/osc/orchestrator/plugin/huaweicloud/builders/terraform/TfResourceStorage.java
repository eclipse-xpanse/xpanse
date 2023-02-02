/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
class TfResourceStorage extends TfResourceSchema {

    TfResourceStorage() {
        oclType = "storage";
        tfType = "huaweicloud_evs_volume";

        input = List.of();

        output = List.of(
                new TfProperty("size", "size"),
                new TfProperty("type", "volume_type"));
    }
}
