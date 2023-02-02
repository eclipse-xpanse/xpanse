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
class TfResourceEip extends TfResourceSchema {

    TfResourceEip() {
        oclType = "internal";
        tfType = "huaweicloud_vpc_eip";

        input = List.of();

        output = List.of(
                new TfProperty("address", "address"));
    }
}