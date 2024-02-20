/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Bean for serializing string in request parameters to ReusbaleCloudResource enum.
 */
@Component
public class DeployResourceKindEnumConverter
        implements Converter<String, DeployResourceKind> {

    @Override
    public DeployResourceKind convert(String kind) {
        return DeployResourceKind.getByValue(kind);
    }
}
