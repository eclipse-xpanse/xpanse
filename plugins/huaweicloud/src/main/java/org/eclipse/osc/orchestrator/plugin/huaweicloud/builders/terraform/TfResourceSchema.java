/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import java.util.List;
import lombok.Data;
import org.eclipse.osc.modules.ocl.loader.data.models.OclResource;

@Data
class TfResourceSchema {

    String oclType;
    String tfType;

    List<TfProperty> input;
    List<TfProperty> output;

    public boolean isMatch(OclResource oclResource, TfStateResource tfStateResource) {
        return getTfType().equals(tfStateResource.getType())
                && oclResource.getName().equals(tfStateResource.getName());
    }
}