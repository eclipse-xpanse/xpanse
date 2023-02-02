/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import lombok.Data;

@Data
class PortPair {

    private long from;
    private long to;

    PortPair(long from, long to) {
        this.from = from;
        this.to = to;
    }
}