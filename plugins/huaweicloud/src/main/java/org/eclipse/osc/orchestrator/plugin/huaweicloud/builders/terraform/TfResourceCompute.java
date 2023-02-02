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
class TfResourceCompute extends TfResourceSchema {

    TfResourceCompute() {
        oclType = "compute";
        tfType = "huaweicloud_compute_instance";

        input = List.of();

        output = List.of(
                new TfProperty("ipv6", "access_ip_v6"),
                new TfProperty("ip", "access_ip_v4"),
                new TfProperty("id", "id"));
    }
}