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
class TfResourceSubnet extends TfResourceSchema {

    TfResourceSubnet() {
        oclType = "subnet";
        tfType = "huaweicloud_vpc_subnet";

        input = List.of();

        output = List.of(
                new TfProperty("cidr", "cidr"),
                new TfProperty("vpc", "vpc_id"));
    }
}
