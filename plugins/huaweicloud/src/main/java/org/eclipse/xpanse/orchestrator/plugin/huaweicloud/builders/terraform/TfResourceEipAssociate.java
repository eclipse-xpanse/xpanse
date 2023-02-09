/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.huaweicloud.builders.terraform;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.modules.ocl.state.OclResource;

@Data
@EqualsAndHashCode(callSuper = true)
class TfResourceEipAssociate extends TfResourceSchema {

    TfResourceEipAssociate() {
        oclType = "internal";
        tfType = "huaweicloud_compute_eip_associate";

        input = List.of();

        output = List.of(new TfProperty("instance_id", "instance_id"),
                new TfProperty("port_id", "port_id"), new TfProperty("public_ip", "public_ip"),
                new TfProperty("id", "id"), new TfProperty("vm", "instance_id"));
    }

    public boolean isMatch(OclResource oclResource, TfStateResource tfStateResource) {
        return oclResource.getType().equals("compute") && ("xpanse-eip-associated-"
                + oclResource.getName()).equals(tfStateResource.getName());
    }
}
