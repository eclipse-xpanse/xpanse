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
class TfResourceSecurityGroupRule extends TfResourceSchema {

    TfResourceSecurityGroupRule() {
        oclType = "security";
        tfType = "huaweicloud_networking_secgroup_rule";

        input = List.of();

        output = List.of(
                new TfProperty("priority", "priority"),
                new TfProperty("cidr", "remote_ip_prefix"),
                new TfProperty("direction", "direction"),
                new TfProperty("ports", "ports"),
                new TfProperty("action", "action"),
                new TfProperty("protocol", "protocol"));
    }
}