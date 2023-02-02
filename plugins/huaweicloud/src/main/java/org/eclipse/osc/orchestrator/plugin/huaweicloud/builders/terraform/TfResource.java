/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import org.eclipse.osc.modules.ocl.loader.data.models.OclResource;

class TfResource extends OclResource {

    TfResource(TfStateResource tfStateResource) {
        TfResourceSchema tfResourceSchema = TfResourceSchemas.getTfResourceSchema(tfStateResource);
        setName(tfStateResource.getName());
        updateWithSchema(tfResourceSchema, tfStateResource);
    }

    private void updateWithSchema(
            TfResourceSchema tfResourceSchema, TfStateResource tfStateResource) {
        var attrs = tfStateResource.getInstances().get(0).getAttributes();
        setId(attrs.get("id").toString());
        setState("active");
        if (tfResourceSchema != null) {
            setType(tfResourceSchema.oclType);
            for (TfProperty key : tfResourceSchema.getOutput()) {
                var attr = attrs.get(key.getTf());
                if (attr == null) {
                    getProperties().put(key.getOcl(), "null");
                } else {
                    getProperties().put(key.getOcl(), attr.toString());
                }
            }
        } else {
            for (var key : attrs.keySet()) {
                if (attrs.get(key) == null) {
                    getProperties().put(key, null);
                } else {
                    getProperties().put(key, attrs.get(key).toString());
                }
            }
        }
    }

    public void update(TfStateResource tfStateResource) {
        TfResourceSchema tfResourceSchema = TfResourceSchemas.getTfResourceSchema(tfStateResource);
        updateWithSchema(tfResourceSchema, tfStateResource);
    }

    public boolean isMatch(TfStateResource tfStateResource) {
        TfResourceSchema tfResourceSchema = TfResourceSchemas.getTfResourceSchema(tfStateResource);
        if (tfResourceSchema != null) {
            return tfResourceSchema.isMatch(this, tfStateResource);
        }
        return false;
    }
}
