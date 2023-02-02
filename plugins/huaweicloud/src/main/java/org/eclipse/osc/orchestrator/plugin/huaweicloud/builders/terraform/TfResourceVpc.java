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
class TfResourceVpc extends TfResourceSchema {

    TfResourceVpc() {
        oclType = "vpc";
        tfType = "huaweicloud_vpc";

        input = List.of();

        output = List.of(
                new TfProperty("id", "id"),
                new TfProperty("cidr", "cidr"),
                new TfProperty("region", "region"));
    }
}
