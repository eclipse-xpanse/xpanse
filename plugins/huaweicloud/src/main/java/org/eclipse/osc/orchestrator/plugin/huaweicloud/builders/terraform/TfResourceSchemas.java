/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import java.util.List;

class TfResourceSchemas {

    private static volatile List<TfResourceSchema> schemas;

    private static List<TfResourceSchema> getInstance() {
        if (schemas == null) {
            synchronized (TfResourceSchemas.class) {
                if (schemas == null) {
                    schemas = List.of(new TfResourceZone(), new TfResourceCompute(),
                            new TfResourceEipAssociate(), new TfResourceStorage(),
                            new TfResourceVpc(),
                            new TfResourceSubnet(), new TfResourceSecurityGroup(),
                            new TfResourceSecurityGroupRule(), new TfResourceEip());
                }
            }
        }
        return schemas;
    }

    public static TfResourceSchema getTfResourceSchema(TfStateResource tfStateResource) {
        List<TfResourceSchema> tfResourceSchemaList =
                getInstance()
                        .stream()
                        .filter(tfResourceSchema
                                -> tfResourceSchema.getTfType().equals(tfStateResource.getType()))
                        .toList();

        if (tfResourceSchemaList.size() < 1) {
            return null;
        }

        return tfResourceSchemaList.get(0);
    }
}