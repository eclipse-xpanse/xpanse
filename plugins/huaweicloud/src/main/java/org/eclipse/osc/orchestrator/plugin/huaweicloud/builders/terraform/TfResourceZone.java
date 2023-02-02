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
class TfResourceZone extends TfResourceSchema {

    TfResourceZone() {
        oclType = "internal";
        tfType = "huaweicloud_availability_zones";

        input = List.of();

        output = List.of(new TfProperty("names"), new TfProperty("state"));
    }
}