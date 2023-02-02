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
class TfResourceSecurityGroup extends TfResourceSchema {

    TfResourceSecurityGroup() {
        oclType = "security";
        tfType = "huaweicloud_networking_secgroup";

        input = List.of();

        output = List.of();
    }
}