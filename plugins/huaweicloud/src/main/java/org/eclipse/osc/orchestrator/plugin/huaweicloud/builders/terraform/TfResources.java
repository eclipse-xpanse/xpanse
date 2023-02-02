/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osc.modules.ocl.loader.data.models.OclResource;

/**
 * Class to convert OCL definition to resources to be created via Terraform.
 */
public class TfResources {

    List<OclResource> resources = new ArrayList<>();

    /**
     * updates the infrastructure to required state.
     *
     * @param tfState required state.
     */
    public void update(TfState tfState) {
        if (tfState == null) {
            return;
        }
        for (TfStateResource tfStateResource : tfState.getResources()) {
            List<OclResource> tfResourceList =
                    resources.stream()
                            .filter(tfResource -> {
                                if (tfResource instanceof TfResource) {
                                    return ((TfResource) tfResource).isMatch(tfStateResource);
                                } else {
                                    return false;
                                }
                            })
                            .toList();
            if (tfResourceList.size() > 0) {
                OclResource oclResource = tfResourceList.get(0);
                ((TfResource) oclResource).update(tfStateResource);
                return;
            }
            OclResource tfResource = new TfResource(tfStateResource);
            resources.add(tfResource);
        }
    }

    public List<OclResource> getResources() {
        return resources;
    }
}